package com.example.nutrivision.data.remote

import com.example.nutrivision.data.auth.awaitTask
import com.example.nutrivision.data.local.NutriVisionDatabase
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.local.entity.UserEntity
import com.example.nutrivision.domain.text.tr
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Kết quả một lần đồng bộ, hiển thị được ngay trên tab Cá nhân. */
data class SyncOutcome(
    val success: Boolean,
    val pushed: Int = 0,
    val pulled: Int = 0,
    val message: String? = null,
    val finishedAt: Long = System.currentTimeMillis(),
)

/**
 * Đồng bộ hai chiều giữa SQLite trên máy và Firestore của chính tài khoản đó.
 *
 * Quy tắc: mỗi bản ghi có `updated_at`, bản nào mới hơn thì thắng (last-write-wins),
 * xoá mềm bằng `deleted_at` để máy kia cũng xoá theo. Không dùng transaction phía
 * Firestore vì dữ liệu nhỏ và mỗi máy chỉ ghi vào tài khoản của mình.
 *
 * Bố cục: `users/{uid}` (hồ sơ), `users/{uid}/meals/{mealId}`,
 * `users/{uid}/meal_items/{itemId}` — meal_items để phẳng vì một món luôn thuộc đúng
 * một bữa qua trường `meal_id`, và như vậy đổi bữa của món không phải di chuyển document.
 */
class FirestoreSync(
    private val db: NutriVisionDatabase,
    private val photoArchive: PhotoArchive? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun sync(userId: String): SyncOutcome = withContext(Dispatchers.IO) {
        runCatching { merge(userId) }.fold(
            onSuccess = { (pushed, pulled) ->
                SyncOutcome(success = true, pushed = pushed, pulled = pulled)
            },
            onFailure = { error ->
                SyncOutcome(success = false, message = describe(error))
            },
        )
    }

    private suspend fun merge(userId: String): Pair<Int, Int> {
        var pushed = 0
        var pulled = 0

        val root = firestore.collection(USERS).document(userId)

        // ---------------------------------------------------------------- hồ sơ
        val localUser = db.userDao().getUser(userId)
        val remoteUser = root.get().awaitTask()
        if (remoteUser.exists()) {
            val remoteUpdated = remoteUser.long("updated_at")
            val localUpdated = localUser?.updatedAt ?: -1L
            when {
                localUser == null || remoteUpdated > localUpdated -> {
                    db.userDao().upsert(SyncMapper.userFromMap(userId, remoteUser.data.orEmpty()))
                    pulled++
                }
                localUpdated > remoteUpdated -> {
                    root.set(SyncMapper.userToMap(localUser)).awaitTask()
                    if (localUser.syncStatus != UserEntity.SYNC_SYNCED) {
                        db.userDao().upsert(localUser.copy(syncStatus = UserEntity.SYNC_SYNCED))
                    }
                    pushed++
                }
            }
        } else if (localUser != null) {
            root.set(SyncMapper.userToMap(localUser)).awaitTask()
            if (localUser.syncStatus != UserEntity.SYNC_SYNCED) {
                db.userDao().upsert(localUser.copy(syncStatus = UserEntity.SYNC_SYNCED))
            }
            pushed++
        }

        // ----------------------------------------------------------------- bữa ăn
        val remoteMeals = root.collection(MEALS).get().awaitTask()
            .documents.associateBy { it.id }
        val localMeals = db.mealDao().allMeals(userId).associateBy { it.id }

        val mealsToPush = mutableListOf<MealEntity>()
        // Một bữa có thể được tạo độc lập trên hai máy → cùng (ngày, bữa) nhưng khác id.
        // Giữ id đang có trên máy này và chuyển món của bữa kia sang đó.
        val mealIdRemap = mutableMapOf<String, String>()

        (localMeals.keys + remoteMeals.keys).forEach { mealId ->
            val local = localMeals[mealId]
            val remote = remoteMeals[mealId]?.data
            val remoteUpdated = remote?.long("updated_at") ?: -1L
            val localUpdated = local?.updatedAt ?: -1L

            when {
                remote != null && remoteUpdated > localUpdated -> {
                    val incoming = SyncMapper.mealFromMap(userId, mealId, remote)
                    val clash = db.mealDao().findMeal(userId, incoming.date, incoming.mealType)
                    if (clash != null && clash.id != mealId) {
                        if (incoming.updatedAt > clash.updatedAt) {
                            db.mealDao().upsertMeal(
                                incoming.copy(
                                    id = clash.id,
                                    createdAt = clash.createdAt,
                                    photoUri = clash.photoUri,
                                )
                            )
                        }
                        mealIdRemap[mealId] = clash.id
                    } else {
                        db.mealDao().upsertMeal(incoming)
                    }
                    pulled++
                }

                local != null && remote != null && localUpdated == remoteUpdated -> {
                    if (local.syncStatus != MealEntity.SYNC_SYNCED) {
                        db.mealDao().setMealSyncStatus(listOf(local.id), MealEntity.SYNC_SYNCED)
                    }
                }

                local != null -> mealsToPush += local
            }
        }

        // ------------------------------------------------------------------ món ăn
        val remoteItems = root.collection(ITEMS).get().awaitTask()
            .documents.associateBy { it.id }
        val localItems = db.mealDao().allItemsOfUser(userId).associateBy { it.id }

        val itemsToPush = mutableListOf<MealItemEntity>()

        (localItems.keys + remoteItems.keys).forEach { itemId ->
            val local = localItems[itemId]
            val remote = remoteItems[itemId]?.data
            val remoteUpdated = remote?.long("updated_at") ?: -1L
            val localUpdated = local?.updatedAt ?: -1L

            when {
                remote != null && remoteUpdated > localUpdated -> {
                    val incoming = SyncMapper.itemFromMap(itemId, remote)
                    // Món của bữa đã bị gộp sang id khác, hoặc bữa chưa có trên máy này.
                    val mealId = mealIdRemap[incoming.mealId] ?: incoming.mealId
                    val mealExists = db.mealDao().getMeal(mealId) != null
                    if (mealExists) {
                        // food_id có thể là món AI tạo trên máy kia, máy này chưa có.
                        val foodExists = incoming.foodId?.let { db.foodDao().getById(it) != null } ?: true
                        db.mealDao().upsertItem(
                            incoming.copy(
                                mealId = mealId,
                                foodId = incoming.foodId.takeIf { foodExists },
                                photoUri = resolvePhoto(userId, itemId, local, remote),
                            )
                        )
                        pulled++
                    }
                }

                local != null && remote != null && localUpdated == remoteUpdated -> {
                    // Hai máy đã giống nhau, không cần ghi gì.
                }

                local != null -> itemsToPush += local
            }
        }

        // ------------------------------------------------------------------ ghi lên
        // Ảnh món nằm trong Firestore (users/{uid}/meal_photos/{itemId}) nên đẩy ảnh
        // trước, rồi mới ghi metadata kèm cờ `has_photo` + `photo_v` để máy kia biết
        // vừa có ảnh, vừa biết ảnh đang ở phiên bản nén nào.
        val photoStates = itemsToPush.associate { item ->
            item.id to ensurePhoto(userId, item, remotePhotoState(remoteItems[item.id]))
        }
        // Ảnh của những món đã đồng bộ từ trước (khi chưa có tính năng này) được bù một lần.
        // Bỏ qua món vừa đẩy ở trên để không nén và gửi trùng hai lần trong cùng một lượt.
        backfillPhotos(
            userId = userId,
            localItems = localItems.values.filter { it.id !in photoStates.keys },
            remoteItems = remoteItems,
        )
        // Máy này có thể đã bị xoá file ảnh (dọn bộ nhớ, cài lại) trong khi máy kia vẫn còn.
        restoreMissingPhotos(userId, localItems.values, remoteItems)

        pushMeals(root.collection(MEALS), mealsToPush)
        pushItems(root.collection(ITEMS), itemsToPush, photoStates)
        pushed += mealsToPush.size + itemsToPush.size

        return pushed to pulled
    }

    /** Trạng thái ảnh của một món trên mây: có ảnh chưa, và nén theo phiên bản nào. */
    private data class PhotoState(val hasPhoto: Boolean, val version: Int)

    private fun remotePhotoState(snapshot: DocumentSnapshot?): PhotoState = PhotoState(
        hasPhoto = snapshot?.get("has_photo") == true,
        version = (snapshot?.get("photo_v") as? Number)?.toInt() ?: 0,
    )

    /**
     * Có cần gửi ảnh lên mây không?
     *
     * Chỉ gửi khi máy này giữ **ảnh gốc** và bản trên mây chưa có, hoặc còn ở phiên bản
     * nén cũ (để ảnh cũ được thay bằng bản nét hơn). Ảnh do máy khác gửi về (đặt tên
     * theo id món) đã nén sẵn nên không nén lại — nén chồng sẽ làm ảnh mờ dần.
     */
    private fun needsUpload(item: MealItemEntity, remote: PhotoState): Boolean {
        val path = item.photoUri?.takeIf { it.isNotBlank() } ?: return false
        val file = File(path)
        if (!file.isFile) return false
        if (remote.hasPhoto && photoArchive?.isArchivedCopy(item.id, file) == true) return false
        return !remote.hasPhoto || remote.version < PhotoArchive.VERSION
    }

    /**
     * Bảo đảm ảnh của món có trên Firestore.
     *
     * Món đã bị xoá (tombstone) thì dọn ảnh để không tốn dung lượng tài khoản; mất mạng
     * thì giữ nguyên trạng thái cũ — lần đồng bộ sau sẽ thử lại.
     */
    private suspend fun ensurePhoto(
        userId: String,
        item: MealItemEntity,
        remote: PhotoState,
    ): PhotoState {
        val archive = photoArchive ?: return remote
        if (item.deletedAt != null) {
            archive.delete(userId, item.id)
            return PhotoState(hasPhoto = false, version = 0)
        }
        if (!needsUpload(item, remote)) return remote
        val uploaded = archive.upload(userId, item.id, File(item.photoUri!!))
        return if (uploaded) PhotoState(true, PhotoArchive.VERSION) else remote
    }

    /**
     * Bù ảnh cho những món đã có trên mây từ trước nhưng chưa từng gửi ảnh lên
     * (`has_photo` còn false/thiếu) hoặc còn ở bản nén cũ. Chỉ ghi thêm cờ, không
     * đụng `updated_at` nên không tạo vòng đồng bộ mới.
     */
    private suspend fun backfillPhotos(
        userId: String,
        localItems: Collection<MealItemEntity>,
        remoteItems: Map<String, DocumentSnapshot>,
    ) {
        val archive = photoArchive ?: return
        localItems.forEach { item ->
            val snapshot = remoteItems[item.id] ?: return@forEach
            if (item.deletedAt != null) return@forEach
            if (!needsUpload(item, remotePhotoState(snapshot))) return@forEach
            if (archive.upload(userId, item.id, File(item.photoUri!!))) {
                runCatching {
                    snapshot.reference.update(
                        mapOf("has_photo" to true, "photo_v" to PhotoArchive.VERSION),
                    ).awaitTask()
                }
            }
        }
    }

    /** Đồng bộ xong phải thấy ảnh: thiếu file thì tải lại từ Firestore. */
    private suspend fun restoreMissingPhotos(
        userId: String,
        localItems: Collection<MealItemEntity>,
        remoteItems: Map<String, DocumentSnapshot>,
    ) {
        val archive = photoArchive ?: return
        localItems
            .filter { item ->
                item.deletedAt == null &&
                    (item.photoUri == null || !File(item.photoUri).isFile) &&
                    remoteItems[item.id]?.get("has_photo") == true
            }
            .forEach { item ->
                val file = archive.download(userId, item.id) ?: return@forEach
                db.mealDao().setItemPhoto(item.id, file.absolutePath)
            }
    }

    /** Giữ ảnh đang có trên máy; nếu món đến từ máy khác và có ảnh thì tải về. */
    private suspend fun resolvePhoto(
        userId: String,
        itemId: String,
        local: MealItemEntity?,
        remote: Map<String, Any?>,
    ): String? {
        val archive = photoArchive ?: return local?.photoUri?.takeIf { File(it).isFile }
        val localFile = local?.photoUri?.let(::File)?.takeIf { it.isFile }
            ?: archive.localFile(itemId).takeIf { it.isFile }
        if (localFile != null) return localFile.absolutePath
        if (remote["has_photo"] != true) return local?.photoUri
        return archive.download(userId, itemId)?.absolutePath ?: local?.photoUri
    }

    private suspend fun pushMeals(
        collection: CollectionReference,
        meals: List<MealEntity>,
    ) {
        meals.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { meal ->
                batch.set(collection.document(meal.id), SyncMapper.mealToMap(meal))
            }
            batch.commit().awaitTask()
            db.mealDao().setMealSyncStatus(chunk.map { it.id }, MealEntity.SYNC_SYNCED)
        }
    }

    private suspend fun pushItems(
        collection: CollectionReference,
        items: List<MealItemEntity>,
        photoStates: Map<String, PhotoState>,
    ) {
        items.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val photo = photoStates[item.id] ?: PhotoState(hasPhoto = false, version = 0)
                batch.set(
                    collection.document(item.id),
                    SyncMapper.itemToMap(
                        item = item,
                        hasPhoto = photo.hasPhoto,
                        photoVersion = photo.version,
                    ),
                )
            }
            batch.commit().awaitTask()
        }
    }

    private fun DocumentSnapshot.long(key: String): Long =
        (get(key) as? Number)?.toLong() ?: 0L

    private fun Map<String, Any?>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: 0L

    private fun describe(error: Throwable): String = when {
        error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> tr(
            "Firestore từ chối truy cập. Kiểm tra rules cho tài khoản này.",
            "Firestore denied access. Check the rules for this account.",
        )
        else -> tr(
            "Đồng bộ thất bại: ${error.message ?: "lỗi không rõ"}",
            "Sync failed: ${error.message ?: "unknown error"}",
        )
    }

    companion object {
        private const val USERS = "users"
        private const val MEALS = "meals"
        private const val ITEMS = "meal_items"

        /** Firestore giới hạn 500 thao tác mỗi batch. */
        private const val BATCH_LIMIT = 400
    }
}
