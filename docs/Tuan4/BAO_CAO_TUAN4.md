# Báo cáo Tuần 4 — Đăng nhập Firebase, camera + AI, đồng bộ Firestore, nhắc nhở giờ ăn

**Dự án:** NutriVision — ứng dụng nhật ký dinh dưỡng (Android, Kotlin + Jetpack Compose)
**Phạm vi:** Tuần 4 — đăng nhập thật, camera + nhận diện món, đồng bộ đám mây, thông báo nhắc bữa ăn. Tất cả dùng dữ liệu thật, không còn dữ liệu mẫu hay chữ giả.

---

## 1. Kết quả đạt được

| Hạng mục | Trước tuần 4 | Sau tuần 4 |
|---|---|---|
| Đăng nhập | Nút bấm vào là xong, `user_id = "local"` | **Firebase Auth thật** (email + mật khẩu), `user_id = Firebase UID`, có xác thực email, điều khoản, nhập lại mật khẩu |
| Dữ liệu tuần 3 | Nằm dưới `user_id = "local"` | **Tự nhận vào tài khoản thật** khi email hồ sơ cũ trùng email đăng nhập (`LegacyAdoption.emailMatches`); không trùng thì để riêng, không trộn |
| Camera | Chip "Sắp có" | **CameraX chụp thật** + **TFLite chạy trên máy** nhận diện món, top-3 kèm độ tin cậy |
| Mô tả món bằng AI | Chưa có | Nút **"Nhờ AI mô tả"** gọi Gemini, trả về món + khẩu phần + dinh dưỡng, lưu thành món riêng (`is_custom = 1`). Thêm **"Chọn ảnh có sẵn"** để dùng ảnh trong thư viện thay vì phải chụp |
| Đồng bộ | Chưa có | **Firestore tự động 2 chiều**, không có nút bấm tay |
| Nhắc nhở | Một công tắc chung, giờ cố định | **Giờ riêng cho từng bữa**, mặc định **tắt**, tự bỏ qua bữa đã ghi, thông báo kèm số kcal còn thiếu |
| Dữ liệu hiển thị | — | Không biết thì "–" / "Chưa có dữ liệu" / "No data", không suy diễn |
| Ngôn ngữ | Việt | **Việt / English** cho toàn bộ chữ mới |

**Chưa làm (đúng phạm vi):** đăng nhập OTP điện thoại và Google Sign-In (theo yêu cầu chỉ dùng email), hiển thị ảnh đã chụp trong nhật ký.

---

## 2. Đăng nhập thật bằng Firebase

### 2.1. Luồng

```
AuthScreen
  ├─ Đăng nhập: email + mật khẩu  ──► AuthRepository.signIn()
  └─ Đăng ký:   email + mật khẩu + nhập lại mật khẩu + checkbox điều khoản
                                   ──► AuthRepository.signUp()  ──► gửi link xác thực email
```

- Mật khẩu tối thiểu **6 ký tự**, kiểm tra bằng `AuthValidation` (thuần Kotlin, test được trên JVM).
- Bắt buộc tick **"Tôi đồng ý với Điều khoản sử dụng"** mới bật nút đăng ký.
- Giữ ô **nhập lại mật khẩu** và báo lỗi ngay dưới ô khi hai lần nhập khác nhau.
- Sau khi đăng ký, app gửi link xác thực và hiện thẻ **"Email chưa xác thực"** ở tab Cá nhân với hai nút *Gửi lại link* / *Tôi đã xác thực*.

### 2.2. Quyết định thiết kế

- **Email chưa xác thực vẫn dùng app được** (đúng yêu cầu: không chặn người dùng), nhưng đồng bộ **không** phụ thuộc trạng thái xác thực — dữ liệu vẫn lên Firestore bình thường.
- Hồ sơ tự đồng bộ email về email tài khoản (`healProfileEmail`), nên badge email chỉ đọc, người dùng không sửa được thành một email khác.
- Lỗi Firebase được dịch sang câu tiếng Việt/Anh dễ hiểu (sai mật khẩu, email đã tồn tại, mật khẩu quá ngắn, mất mạng…), không hiện mã lỗi thô.

---

## 3. Camera + nhận diện món

### 3.1. CameraX

- Xin quyền camera đúng lúc mở tab **Chụp món**; từ chối thì hiện hướng dẫn cấp lại, không treo màn hình.
- Preview + chụp ảnh bằng `CameraController`, ảnh lưu vào bộ nhớ riêng của app.
- Thanh trạng thái đổi màu icon theo nền camera (`SystemBarsAppearance(useDarkIcons = !showCamera)`), chip bữa ăn dùng nhãn ngắn để không bị xuống dòng.

### 3.2. TFLite trên máy

| Thông số | Giá trị |
|---|---|
| Kích thước đầu vào | 224 × 224, RGB, bilinear |
| Kiểu dữ liệu | float32, **0–255, không chia 255** (khớp `test_ai/predict.py`) |
| Ngưỡng tự tin | 0.7 — dưới ngưỡng thì hiện "Chưa chắc chắn" và mời chọn tay |
| Kết quả | Top-3 lớp, tên tiếng Việt lấy từ `labels.txt` + `class_names_vi.json` |
| Phạm vi | 131 món có `model_class`, model **không có lớp đồ uống** (báo trung thực khi ảnh là đồ uống) |

Kết quả nhận diện chỉ là **gợi ý**: người dùng luôn có thể chọn lại món trong danh sách 145 món.

### 3.3. Gemini (tuỳ chọn)

- Chỉ gọi khi người dùng bấm **"Nhờ AI mô tả"** — không tự gọi, không tốn quota.
- **Không chỉ hiện khi model chưa chắc chắn**: model có thể đoán sai món mà vẫn cho điểm cao (ảnh mờ, món lạ, góc chụp lạ), nên khi model tự tin vẫn có dòng *"Nhận diện sai? Nhờ AI xem lại"* ngay dưới nút lưu — AI luôn là lối thoát, nhưng chỉ chạy khi người dùng chủ động bấm.
- Khoá đọc từ `local.properties` (`gemini.api.key`) hoặc biến môi trường, đưa vào `BuildConfig.GEMINI_API_KEY`; **không có khoá thì nút bị ẩn**, app vẫn chạy đầy đủ bằng dữ liệu có sẵn. File `local.properties` đã nằm trong `.gitignore`, khoá không lên git.
- Món do Gemini trả về được lưu với `is_custom = 1` và quy đổi dinh dưỡng theo /100 g để khẩu phần chỉnh được như món thường.
- Prompt yêu cầu Gemini trả **JSON theo `responseSchema`** (tên món, khẩu phần, kcal, đạm/carb/béo, ghi chú) và từ chối khi ảnh không có món ăn — nhờ vậy app hiện đúng câu "AI không nhận ra món trong ảnh" thay vì bịa số.

### 3.4. Chọn ảnh có sẵn

- Hai nút **"Chọn ảnh có sẵn"**: một ở màn xin quyền camera, một ở màn chụp — người dùng không cần cấp quyền camera vẫn thử được AI.
- Dùng **Photo Picker** (`ActivityResultContracts.PickVisualMedia`), **không xin quyền đọc thư viện**; hệ thống chỉ trả về đúng ảnh người dùng chọn.
- Ảnh được thu nhỏ còn tối đa **1600 px**, nén JPEG 90 % và lưu vào `filesDir/photos`, sau đó đi **đúng pipeline của ảnh chụp**: TFLite trên máy trước, chỉ gọi Gemini khi bấm "Nhờ AI mô tả". Ảnh hỏng/không đọc được thì báo "Không đọc được ảnh đã chọn", không treo màn hình.

### 3.5. Lỗi đã tìm ra và sửa: Gemini luôn trả 404

Lần chạy thật đầu tiên, khoá đúng nhưng app nhận `Gemini trả về lỗi 404`. Nguyên nhân: `GeminiFoodService.post()` gọi `…/v1beta/models?key=…`, **thiếu `/{model}:generateContent`** trong URL. Đã sửa thành:

```
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=…
```

Kiểm chứng lại bằng REST và bằng chính app: ảnh `pho_bo.webp` → TFLite đoán "Phở" 56 % → bấm "Nhờ AI mô tả" → Gemini trả **"Phở bò", 400 kcal cho 500 g, 35 g đạm / 50 g carb / 15 g béo**, badge "AI ước lượng" + ghi chú tiếng Việt; tăng khẩu phần lên 550 g thì app tự tính lại thành 450 kcal (35/60/20 g) và lưu vào bữa trưa. Ảnh `tuan4_pick_photo.png`, `tuan4_gemini_result.png`, `tuan4_ai_saved.png`.

---

## 4. Đồng bộ Firestore tự động

### 4.1. Mô hình dữ liệu trên Firestore

```
users/{uid}                     ← hồ sơ + mục tiêu dinh dưỡng
users/{uid}/meals/{mealId}      ← bữa ăn
users/{uid}/meal_items/{itemId}  ← món trong bữa (để phẳng, không lồng dưới meals)
```

- Mọi bản ghi đều có `updated_at`; xoá là **xoá mềm** bằng `deleted_at` (tombstone) để máy khác biết mà xoá theo.
- Gộp theo **last-write-wins** trên `updated_at`.
- Ghi theo batch, tối đa **400 thao tác** một lượt để không vượt giới hạn Firestore.
- `firestore.rules` chỉ cho chủ tài khoản đọc/ghi `users/{uid}/**`, đã publish lên project `nutrivision-9c3a3`.

### 4.2. Khi nào tự đồng bộ

| Thời điểm | Cơ chế |
|---|---|
| Mở app / đăng nhập | Đồng bộ ngay, **giới hạn 6 giây** để màn hình chờ không phụ thuộc mạng |
| Sau mỗi thay đổi dữ liệu | Nghe **"vân tay"** Room (`COUNT(*)` + `MAX(updated_at)` của `users`, `meals`, `meal_items`), gộp bằng **debounce 2 giây** rồi đẩy lên |
| Quay lại app sau > 5 phút | Kéo dữ liệu máy khác về trong im lặng |

- **Không còn nút "Đồng bộ ngay"**: tab Cá nhân chỉ hiển thị trạng thái *"Đang đồng bộ…"* / *"Đã đồng bộ HH:mm"* và câu lỗi cụ thể nếu hỏng.
- Chống vòng lặp: đồng bộ chỉ đổi `sync_status`, **không** đổi `updated_at`, nên không tự kích hoạt lại chính nó.
- Xung đột bữa trùng ngày + loại giữa hai máy được **gộp về một bữa**, món được chuyển sang bữa đó thay vì nhân đôi.
- Món trỏ tới `food_id` không tồn tại trên máy kia thì bị bỏ, không tạo bản ghi mồ côi.

### 4.3. Kiểm chứng thật trên máy thật

1. Thêm món "Ba ba nấu chuối đậu" (417 kcal) vào bữa sáng → không bấm gì thêm.
2. Sau ~2 giây, `last_sync_at` trong `shared_prefs` nhảy từ **10:57:18** sang **10:59:36** (đúng thời điểm lưu món + debounce).
3. `adb uninstall com.example.nutrivision` → cài lại → đăng nhập lại cùng tài khoản.
4. Hồ sơ (Minh, mục tiêu 2.236 kcal) và món bữa sáng **quay về đủ**, không thao tác tay — ảnh `tuan4_sync_restored.png`.

### 4.4. Ảnh món đồng bộ qua Firestore (không dùng Firebase Storage)

Firebase Storage **không dùng được** cho project này: bucket chưa được tạo (upload trả HTTP 404) và muốn bật Storage phải nâng lên gói **Blaze có trả phí**. Vì vậy ảnh món được đồng bộ bằng chính Firestore:

```
users/{uid}/meal_photos/{itemId}   ← document { data: "<base64 JPEG>", updated_at }
```

- Ảnh được **nén trước khi gửi**: cạnh dài nhất 720 px, JPEG chất lượng 70 → thường **40–90 KB**, nằm an toàn dưới giới hạn 1 MiB của một document Firestore. Ảnh nén vượt 200 KB thì bỏ qua thay vì đẩy document quá lớn.
- `firestore.rules` hiện có `match /{collection}/{docId}` dưới `users/{userId}` đã phủ `meal_photos`, **không cần deploy luật mới**.
- Document metadata của món chỉ giữ cờ `has_photo`, nên mỗi lần sửa khẩu phần **không** kéo theo tải/đẩy lại ảnh.
- Chỉ đọc **đúng document của món đang thiếu file**, không quét cả collection: máy thứ hai mở app sẽ tải ảnh về `files/photos/<itemId>.jpg`.
- Món bị xoá (tombstone) thì ảnh trên Firestore cũng bị xoá, không tốn dung lượng tài khoản.
- Mọi thao tác ảnh là **best-effort**: mất mạng hay lỗi quyền thì ghi log `PhotoArchive` và bỏ qua; nhật ký trên máy vẫn hiển thị bình thường vì ảnh gốc còn trong `filesDir`.


---

## 5. Nhắc nhở giờ ăn do người dùng tự chỉnh

### 5.1. Giao diện

Tab Cá nhân → **"Nhắc nhở giờ ăn"** mở hộp thoại:

```
Bữa sáng    07:00   [tắt]
Bữa trưa    11:30   [tắt]
Bữa tối     18:30   [tắt]
                        [Xong]
```

- Mỗi bữa một **công tắc** và một **nút giờ**; bấm giờ mở `TimePickerDialog` 24 giờ của hệ thống.
- **Mặc định tắt ở lần cài mới**: không tự xin quyền thông báo, không bắn thông báo ngoài ý muốn. Giờ 7:00 / 11:30 / 18:30 chỉ là gợi ý khi người dùng bật bữa đó.
- Người dùng đã bật từ bản trước thì giữ nguyên lựa chọn cũ (đọc khoá `enabled` cũ), không bị tắt ngầm.
- Dòng "Nhắc nhở giờ ăn" ngoài danh sách hiển thị gọn các mốc đang bật, ví dụ `07:00 · 11:30 · 18:30`, hoặc "Tắt".

### 5.2. Phần "thông minh"

- **Chỉ nhắc bữa đang bật**; tắt hết thì không hẹn việc gì.
- Mốc kế tiếp được tính từ danh sách bữa đang bật, **kể cả sang ngày mai**, nên không có chuyện hẹn sai giờ.
- Worker chạy xong **tự hẹn mốc kế** → không đánh thức máy mỗi 15 phút như `PeriodicWorkRequest`.
- Trước khi bắn thông báo, worker kiểm tra **hôm nay bữa đó đã có món chưa**: có rồi thì im lặng, chỉ hẹn bữa sau.
- Nội dung thông báo lấy **dữ liệu thật**: `"Còn 2236 kcal so với mục tiêu hôm nay."` (hoặc "đã đủ mục tiêu"), không có câu chung chung.
- Từ Android 13, quyền thông báo được xin **đúng lúc bật công tắc**, không xin lung tung khi mở app.
- Nếu quyền thông báo đang bị tắt ở cấp hệ điều hành, hộp thoại nhắc nhở hiện dòng cảnh báo đỏ *"Thông báo đang bị tắt cho NutriVision nên nhắc nhở sẽ không hiện."* kèm nút **"Mở cài đặt thông báo"** (`Settings.ACTION_APP_NOTIFICATION_SETTINGS`). Đây là lỗi thật gặp khi cài lại app: quyền chưa được cấp nên nhắc nhở im lặng dù công tắc đang bật.

### 5.3. Lỗi đã tìm ra và sửa

Ban đầu bật nhắc nhở nhưng **không có job nào được lên lịch**. Nguyên nhân: `ReminderScheduler` tự tạo một `ReminderSettings` riêng, đọc phải giá trị cũ trong bộ nhớ đệm. Nay `AppContainer` truyền **cùng một instance** cho UI và scheduler.

Kiểm chứng sau khi sửa:

```
WM-DiagnosticsWrkr: 140befa4…  MealReminderWorker  ENQUEUED  meal_reminder
```

Đến 10:55 thông báo bắn thật: tiêu đề *"Đến giờ ăn sáng"*, nội dung *"Ghi bữa sáng mất 20 giây - mở NutriVision và thêm món. Còn 2236 kcal so với mục tiêu hôm nay."* — ảnh `tuan4_notification.png`.

---

## 6. Quy tắc "chỉ dùng dữ liệu thật" và song ngữ

- Không có số liệu bịa: thiếu hồ sơ thì "Chưa có dữ liệu", thiếu chỉ số thì "–", tiếng Anh là "No data".
- Bỏ dòng placeholder **"Trợ giúp & phản hồi / Sắp có"**; dòng "Về NutriVision" lấy `BuildConfig.VERSION_NAME` thật.
- Mọi chữ mới đều đi qua `tr("vi", "en")`, đổi ngôn ngữ trong Cá nhân là đổi ngay cả hộp thoại nhắc nhở, thông báo và câu lỗi đồng bộ.
- Lỗi đồng bộ tự động **không bật snackbar** (tránh làm phiền); chỉ hiện một dòng đỏ dưới mục "Đồng bộ" ở tab Cá nhân.
- Hàng **"Về NutriVision"** bấm được: mở hộp thoại hiện **thông tin thật** — logo, `BuildConfig.VERSION_NAME` + `VERSION_CODE`, email đang đăng nhập kèm trạng thái xác thực, và 4 gạch đầu dòng mô tả đúng những gì app làm (nhận diện trên máy, ảnh chỉ gửi Gemini khi người dùng bấm, đồng bộ tự động, tài khoản riêng tư). Cuối trang là dòng miễn trừ "số liệu chỉ để tham khảo".
- Ảnh món trong nhật ký và màn sửa khẩu phần **ưu tiên đúng ảnh người dùng vừa chụp/chọn** (`photoUri`), chỉ khi món không có ảnh mới rơi về ảnh danh mục trong assets, cuối cùng mới là khung gradient + icon.
- Xoá món hoặc xoá cả bữa thì **file ảnh cũng bị xoá** nếu không còn món nào khác dùng (`countItemsUsingPhoto`), tránh để lại rác trong bộ nhớ máy.

---

## 7. Danh sách file

### 7.1. Thêm mới

| File | Nội dung |
|---|---|
| `data/ai/FoodClassifier.kt` | Chạy TFLite, tiền xử lý 224×224, top-3, ngưỡng 0.7 |
| `data/remote/FirestoreSync.kt` | Đồng bộ 2 chiều, last-write-wins, tombstone, batch |
| `data/remote/SyncMapper.kt` | Map Entity ⇄ Firestore document |
| `data/remote/GeminiFoodService.kt` | Gọi Gemini mô tả món + quy đổi /100 g (endpoint `/{model}:generateContent`) |
| `data/prefs/ReminderSettings.kt` | Giờ + bật/tắt từng bữa (mặc định **tắt**), migrate khoá `enabled` cũ |
| `data/remote/PhotoArchive.kt` | Nén ảnh ≤ 720 px q70 rồi mã hoá base64 lưu `users/{uid}/meal_photos/{itemId}`; tải/xoá theo từng document |
| `data/local/relation/DataFingerprint.kt` | "Vân tay" bảng để phát hiện thay đổi |
| `notifications/MealSlot.kt` | 3 bữa, `ReminderPlanner.next()`, nội dung thông báo động |
| `notifications/ReminderScheduler.kt` | Hẹn mốc kế tiếp, tạo kênh, bắn thông báo |
| `notifications/MealReminderWorker.kt` | Bỏ qua bữa đã ghi, tính kcal còn lại, hẹn tiếp |
| `ui/viewmodel/CameraViewModel.kt` | Trạng thái chụp/nhận diện/gọi Gemini |
| `res/drawable/ic_meal_reminder.xml` | Icon thông báo |
| `app/src/test/.../ReminderPlannerTest.kt` | 7 test cho lịch nhắc |
| `app/src/androidTest/.../FoodClassifierInstrumentedTest.kt` | 2 test chạy model thật trên máy |
| `firestore.rules`, `firebase.json` | Luật bảo mật + cấu hình deploy |

### 7.2. Sửa đổi

| File | Thay đổi |
|---|---|
| `MainActivity.kt` | Nối state mới, `onAppForeground()` khi quay lại app |
| `ui/viewmodel/AppViewModel.kt` | `startAutoSync()` (debounce 2 s), `onAppForeground()`, `lastSyncError`, bỏ `syncNow`, `setReminderEnabled/Time` |
| `ui/screens/ProfileScreen.kt` | Bỏ nút đồng bộ tay, thêm `ReminderDialog` + TimePicker, cảnh báo khi quyền thông báo bị tắt, bỏ placeholder, hàng "Về NutriVision" bấm được mở `AboutDialog` |
| `ui/components/FoodVisuals.kt` | `FoodAvatar`/`FoodIdAvatar` nhận `photoPath`: ảnh thật của người dùng → ảnh assets → gradient + icon |
| `ui/screens/DiaryScreen.kt` | Truyền `photoUri` của món xuống avatar trong nhật ký |
| `ui/screens/MealDetailScreen.kt`, `ui/viewmodel/MealDetailViewModel.kt` | Header màn sửa khẩu phần hiện đúng ảnh thật của món |
| `data/remote/FirestoreSync.kt` | Ghi/tải/xoá ảnh qua `PhotoArchive`, cờ `has_photo`, bù ảnh cũ, tải lại ảnh bị thiếu |
| `data/local/dao/MealDao.kt` | `countItemsUsingPhoto` (dọn file khi xoá món), `setItemPhoto` (gắn lại ảnh sau khi tải) |
| `data/repository/MealRepository.kt` | `releasePhoto()` khi xoá món/xoá bữa |
| `ui/screens/MainShell.kt` | Truyền state nhắc nhở + lỗi đồng bộ xuống Cá nhân |
| `ui/screens/CameraScreen.kt` | Sửa icon thanh trạng thái, chip nhãn ngắn, thêm **"Chọn ảnh có sẵn"** (Photo Picker) + `importPickedPhoto()` nén ảnh ≤ 1600 px |
| `data/local/dao/MealDao.kt` | `countMealTypeItems`, `totalCalories`, 2 hàm `observe*Fingerprint` |
| `data/local/dao/UserDao.kt` | `observeFingerprint` |
| `data/prefs/SessionManager.kt` | `lastSyncError`, `markSyncFailed`, xoá lỗi khi thành công |
| `data/repository/FoodRepository.kt` | `saveAiFood` |
| `data/repository/MealRepository.kt` | `addItem` nhận `source/confidence/photoUri` |
| `di/AppContainer.kt` | Truyền chung `ReminderSettings` cho scheduler |
| `app/build.gradle.kts`, `libs.versions.toml` | CameraX 1.6.2, TFLite 2.17.0, WorkManager 2.12.0, `BuildConfig.GEMINI_API_KEY` |
| `AndroidManifest.xml` | Quyền CAMERA, POST_NOTIFICATIONS |
| `.gitignore` | Bỏ qua `.kotlin/` |

---

## 8. Kiểm thử

### 8.1. Unit test — **39/39 xanh**

| Lớp test | Số test | Nội dung |
|---|---|---|
| `AuthValidationTest` | 13 | Email, mật khẩu ≥ 6, nhập lại mật khẩu, điều khoản |
| `LegacyAdoptionTest` | 5 | Nhận dữ liệu tuần 3 chỉ khi email trùng |
| `NutritionCalculatorTest` | 8 | BMR / TDEE / mục tiêu calo và macro |
| `VietnameseTextTest` | 5 | Bỏ dấu, tìm không dấu |
| `ReminderPlannerTest` | 7 | Bữa gần nhất, qua giờ thì sang mai, tắt bữa, giờ tuỳ chỉnh, tắt hết |
| `ExampleUnitTest` | 1 | Mặc định của project |

```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL
```

### 8.2. Instrumented test — **3/3 xanh**

`connectedDebugAndroidTest` chạy model thật trên emulator:

- Top-1 trên 131 ảnh mẫu trong assets **≥ 50%** (nếu tiền xử lý sai, ví dụ chia 255, kết quả tụt về ~0% và test fail).
- Ảnh xám trơn **không** được vượt ngưỡng tin cậy.
- Test mẫu của project.

### 8.3. Kiểm thử tay trên máy thật (CPH2819, 360 dp)

| Việc | Kết quả |
|---|---|
| Đăng ký + nhận link xác thực | Có thẻ "Email chưa xác thực", gửi lại được |
| Chụp món + nhận diện | Top-3 hiện đúng khung, món lưu vào bữa đã chọn |
| **Chọn ảnh có sẵn** (`pho_bo.webp`) | TFLite đoán "Phở" 56 %; Gemini trả "Phở bò" + dinh dưỡng; lưu vào bữa trưa, tổng calo cộng đúng |
| Model tự tin nhưng nghi sai | 96 % vẫn có dòng "Nhận diện sai? Nhờ AI xem lại" → Gemini trả "Hot dog" 300 kcal, badge "AI ước lượng" |
| Thêm món → tự đồng bộ | `last_sync_at` đổi sau ~2 s, không bấm gì |
| Gỡ + cài lại + đăng nhập | Hồ sơ và món cũ quay về từ Firestore |
| **Ảnh thật trong nhật ký** | Màn sửa khẩu phần hiện đúng ảnh người dùng vừa chọn, **không** phải ảnh danh mục `com_tam.webp` |
| **Xoá món có ảnh** | DB có `deleted_at`; file ảnh biến mất khỏi `files/photos` (10 → 9 file) |
| Đổi giờ nhắc 10:55, chờ | Thông báo bắn đúng phút, kèm kcal còn thiếu |
| Bật/tắt từng bữa | Job `meal_reminder` được thay đúng mốc kế tiếp |
| Cài mới (chưa cấp quyền thông báo) | Nhắc nhở mặc định tắt; hộp thoại hiện cảnh báo + nút mở cài đặt |

---

## 9. Ảnh minh chứng

| Ảnh | Nội dung |
|---|---|
| `docs/screenshots/tuan4_auth_login.png` | Đăng nhập email thật |
| `docs/screenshots/tuan4_email_verify.png` | Thẻ nhắc xác thực email |
| `docs/screenshots/tuan4_camera.png` | CameraX đang mở trong app |
| `docs/screenshots/tuan4_recognition.png` | Kết quả nhận diện top-3 |
| `docs/screenshots/tuan4_reminder_dialog.png` | Hộp thoại chỉnh giờ từng bữa |
| `docs/screenshots/tuan4_notification.png` | Thông báo thật lúc 10:55 kèm kcal còn thiếu |
| `docs/screenshots/tuan4_sync_restored.png` | Cài lại app, đăng nhập, dữ liệu về đủ |
| `docs/screenshots/tuan4_pick_photo.png` | "Chọn ảnh có sẵn" → TFLite nhận "Phở" 56 % |
| `docs/screenshots/tuan4_gemini_result.png` | Gemini trả "Phở bò" + dinh dưỡng, badge "AI ước lượng" |
| `docs/screenshots/tuan4_ai_saved.png` | Món AI đã nằm trong nhật ký bữa trưa |

---

## 10. Hạn chế và việc còn lại

1. **Khoá Gemini không nằm trong repo** (đúng quy ước bảo mật): máy khác muốn thử AI phải tự thêm `gemini.api.key` vào `local.properties` rồi build lại; chưa có khoá thì nút "Nhờ AI mô tả" tự ẩn và app vẫn chạy đủ bằng model trên máy. Chất lượng mô tả phụ thuộc hạn mức/khoá của người dùng.
2. Model TFLite đạt khoảng **71% top-1** trên tập test gốc; ảnh chụp thực tế trong nhà có thể thấp hơn — vì vậy app luôn cho chọn lại món và hiện "Chưa chắc chắn" khi độ tin cậy < 0.7.
3. Model **không có lớp đồ uống**; ảnh đồ uống thường bị đoán sang món ăn gần nhất.
4. Ảnh món đồng bộ qua Firestore nên phải **nén** (≤ 720 px, q70). Máy thứ hai nhận ảnh nén, không phải bản gốc full độ phân giải; ảnh gốc vẫn nằm trên máy đã chụp.
5. Nhắc nhở dựa trên giờ máy; nếu người dùng đổi múi giờ hoặc tắt tiết kiệm pin mạnh, hệ điều hành có thể trễ vài phút.
6. Đồng bộ cần mạng; khi mất mạng app vẫn dùng bình thường và tự thử lại ở lần thay đổi kế tiếp hoặc lần mở app sau. Ảnh chưa gửi được sẽ được bù ở lần đồng bộ sau.

**Kế hoạch tuần 5–6:** kiểm chứng vòng đồng bộ ảnh giữa hai thiết bị (xoá file ảnh trên máy → mở app → ảnh tự tải lại), chụp bổ sung ảnh minh chứng hộp thoại "Về NutriVision", hoàn thiện báo cáo và chuẩn bị demo.
