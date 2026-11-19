package com.example.nutrivision.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.nutrivision.data.auth.awaitTask
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * Đồng bộ ảnh món mà **không cần Firebase Storage** (Storage đòi gói Blaze).
 *
 * Ảnh được nén còn tối đa 720 px, JPEG chất lượng 70 (~40–90 KB), mã hoá base64 rồi lưu
 * vào document `users/{uid}/meal_photos/{itemId}` — nằm dưới giới hạn 1 MiB của Firestore
 * và dùng đúng rules sẵn có (`match /{collection}/{docId}` của chủ tài khoản).
 *
 * Chỉ đọc đúng document cần dùng, không bao giờ quét cả collection: máy thứ hai chỉ tải
 * ảnh của món nào thật sự thiếu file, và mỗi lần đổi khẩu phần không kéo theo tải ảnh.
 *
 * Mọi thao tác đều "best-effort": mất mạng hay lỗi quyền thì trả false/null, nhật ký trên
 * máy vẫn hiển thị bình thường vì ảnh gốc vẫn nằm trong filesDir.
 */
class PhotoArchive(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    private val appContext = context.applicationContext

    /** Ảnh tải về nằm cùng thư mục với ảnh camera chụp (xem CameraScreen). */
    fun localFile(itemId: String): File = File(photosDir(), "$itemId.jpg")

    suspend fun upload(userId: String, itemId: String, source: File): Boolean =
        withContext(Dispatchers.IO) {
            if (userId.isBlank() || itemId.isBlank() || !source.isFile) return@withContext false
            val encoded = encode(source) ?: return@withContext false
            runCatching {
                collection(userId).document(itemId)
                    .set(mapOf("data" to encoded, "updated_at" to System.currentTimeMillis()))
                    .awaitTask()
                true
            }.getOrElse { error ->
                Log.w(TAG, "upload $itemId thất bại: ${error.message}")
                false
            }
        }

    /** Tải ảnh về máy; trả null nếu món không có ảnh hoặc đang offline. */
    suspend fun download(userId: String, itemId: String): File? = withContext(Dispatchers.IO) {
        if (userId.isBlank() || itemId.isBlank()) return@withContext null
        val snapshot = runCatching {
            collection(userId).document(itemId).get().awaitTask()
        }.getOrElse { error ->
            Log.w(TAG, "download $itemId thất bại: ${error.message}")
            return@withContext null
        }
        val encoded = snapshot.getString("data") ?: return@withContext null
        val bytes = runCatching { Base64.decode(encoded, Base64.NO_WRAP) }.getOrNull()
            ?: return@withContext null
        val target = localFile(itemId)
        runCatching {
            target.parentFile?.mkdirs()
            target.writeBytes(bytes)
            target
        }.getOrNull()
    }

    /** Dọn ảnh của món đã bị xoá. Không có ảnh cũng coi như xong. */
    suspend fun delete(userId: String, itemId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || itemId.isBlank()) return@withContext
        runCatching {
            collection(userId).document(itemId).delete().awaitTask()
        }.onFailure { error ->
            Log.w(TAG, "delete $itemId thất bại: ${error.message}")
        }
        Unit
    }

    private fun collection(userId: String) = firestore.collection(USERS).document(userId)
        .collection(PHOTOS)

    /**
     * Nén ảnh: giữ đúng chiều, thu nhỏ về đúng [MAX_SIDE] ở cạnh dài rồi JPEG hoá.
     *
     * `inSampleSize` chỉ giảm theo luỹ thừa 2 (1, 2, 4, 8…) nên một mình nó thường cho
     * ảnh nhỏ hơn mức cần (ảnh 1632 px chia 4 còn 408 px). Vì vậy chỉ dùng nó để hạ
     * bitmap xuống gần đích, sau đó scale chính xác bằng `createScaledBitmap`.
     * Ảnh gốc nhỏ hơn [MAX_SIDE] thì giữ nguyên, không phóng to.
     */
    private fun encode(source: File): String? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Chỉ chia đôi khi vẫn còn lớn hơn gấp đôi đích, để bước scale sau là thu nhỏ
        // (giảm mịn) chứ không phải phóng to làm mờ ảnh.
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded: Bitmap = BitmapFactory.decodeFile(
            source.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample },
        ) ?: return null

        val longest = maxOf(decoded.width, decoded.height)
        val bitmap = if (longest > MAX_SIDE) {
            val ratio = MAX_SIDE.toFloat() / longest
            val targetWidth = (decoded.width * ratio).roundToInt().coerceAtLeast(1)
            val targetHeight = (decoded.height * ratio).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
                .also { if (it !== decoded) decoded.recycle() }
        } else {
            decoded
        }

        val out = ByteArrayOutputStream(IMAGE_BUDGET_BYTES)
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        bitmap.recycle()
        val bytes = out.toByteArray()
        if (bytes.size > IMAGE_BUDGET_BYTES) return null
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrElse { error ->
        Log.w(TAG, "encode ${source.name} thất bại: ${error.message}")
        null
    }

    /** Ảnh tải từ Firestore về được đặt tên theo id món; ảnh gốc của người dùng tên uuid. */
    fun isArchivedCopy(itemId: String, file: File): Boolean = file.name == "$itemId.jpg"

    private fun photosDir(): File = File(appContext.filesDir, DIR).apply { mkdirs() }

    companion object {
        private const val TAG = "PhotoArchive"

        /**
         * Phiên bản thuật toán nén. Tăng số này khi cách nén thay đổi để những máy đã
         * đồng bộ tự gửi lại ảnh nét hơn thay vì giữ bản cũ trên Firestore.
         */
        const val VERSION = 2

        /** Khớp với thư mục ảnh của CameraScreen để mọi ảnh món nằm một chỗ. */
        private const val DIR = "photos"
        private const val USERS = "users"
        private const val PHOTOS = "meal_photos"

        /** Cạnh dài nhất sau khi nén — đủ nét ở khung 76 dp, nhỏ hơn 1 MiB của Firestore. */
        private const val MAX_SIDE = 720
        private const val QUALITY = 70

        /** Trần kích thước ảnh sau khi nén; vượt thì bỏ qua thay vì đẩy document quá lớn. */
        private const val IMAGE_BUDGET_BYTES = 200 * 1024
    }
}
