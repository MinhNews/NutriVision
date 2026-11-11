# Báo cáo Tuần 3 — Cơ sở dữ liệu Room & kiến trúc MVVM

**Dự án:** NutriVision — ứng dụng nhật ký dinh dưỡng (Android, Kotlin + Jetpack Compose)
**Phạm vi:** Tuần 3 — lưu trữ thật bằng Room, kiến trúc MVVM, CRUD đầy đủ, không còn số liệu giả trong UI.

---

## 1. Kết quả đạt được

| Hạng mục | Trước tuần 3 | Sau tuần 3 |
|---|---|---|
| Đăng nhập | Bấm là vào, không lưu ai | Có bản ghi `users` trong DB (`user_id = "local"`), phiên đăng nhập lưu bằng SharedPreferences |
| Hồ sơ | Ô nhập không lưu | Lưu thật vào `users`, tính **BMR / TDEE / mục tiêu calo + 3 macro**, mở lại vẫn còn |
| Trang chủ | Số cứng "1.250 kcal", "45g / 110g" | **100% số thật** lấy từ DB theo mục tiêu và bữa ăn đã ghi |
| 4 bữa trong ngày | 4 thẻ tĩnh | Đọc từ `meals` + `meal_items`, hiện tên món và tổng calo từng bữa |
| Chi tiết món | Tên món cứng | Đọc món thật trong `foods`; kéo gram → calo/đạm/carb/béo đổi theo; thêm/sửa/xoá được |
| Tìm món | Chưa có | Tìm trong **145 món**, gõ không dấu vẫn ra ("bun bo" → Bún bò Huế) |
| Thống kê | Số cứng | Tính từ dữ liệu đã lưu: số ngày có ghi bữa, calo trung bình, biểu đồ **7 ngày hoặc theo tháng** |
| Chọn ngày | Chỉ xem hôm nay | Dải 7 ngày + **lịch tháng/năm** để mở lại bất kỳ ngày nào đã ghi |
| Sang ngày mới | Phải tắt mở app | **Tự chuyển ngày** theo đồng hồ máy, hôm qua vẫn còn trong lịch sử |
| Giao diện | Một bảng màu sáng | **Sáng / tối / theo hệ thống**, chọn trong Cá nhân và ghi nhớ lựa chọn |
| Ngôn ngữ | Chỉ tiếng Việt | **Tiếng Việt / English**, đổi ngay trong Cá nhân, mặc định tiếng Việt, ghi nhớ lựa chọn |
| Tắt app mở lại | Mất hết | **Dữ liệu, giao diện và ngôn ngữ còn nguyên** (đã kiểm chứng trên emulator) |

**Chưa làm (đúng phạm vi tuần 4–5):** chụp ảnh thật, model TFLite nhận diện, gọi Gemini, đồng bộ Firebase, đăng nhập thật.
Các phần này hiện là placeholder trung thực ("Tính năng đang được hoàn thiện"), không giả lập kết quả.

---

## 2. Kiến trúc MVVM

```
┌──────────────────────────────────────────────────────────────┐
│  VIEW (Compose)                                              │
│  MainActivity → MainShell → Dashboard / Diary / Camera /      │
│  Stats / Profile + overlay Search / MealDetail / EditProfile  │
│  • chỉ render UiState, không tính toán dinh dưỡng             │
│  • gửi sự kiện qua lambda / ViewModel function                │
└───────────────────────────┬──────────────────────────────────┘
                            │ collectAsStateWithLifecycle()
┌───────────────────────────▼──────────────────────────────────┐
│  VIEWMODEL                                                    │
│  App / Dashboard / Diary / Search / MealDetail / Stats        │
│  • map Repository Flow → StateFlow<UiState>                    │
│  • giữ state nhập liệu (query, gram, bữa, ngày)                │
│  • gọi Repository trong viewModelScope                         │
└───────────────────────────┬──────────────────────────────────┘
                            │ suspend / Flow
┌───────────────────────────▼──────────────────────────────────┐
│  REPOSITORY (data)                                            │
│  UserRepository / FoodRepository / MealRepository              │
│  • MealRepository: transaction, soft delete, UUID id           │
│  • map Entity ↔ Domain model                                  │
└───────────────────────────┬──────────────────────────────────┘
                            │ DAO
┌───────────────────────────▼──────────────────────────────────┐
│  ROOM (SQLite) — nguồn sự thật duy nhất                       │
│  users · foods · meals · meal_items                           │
└──────────────────────────────────────────────────────────────┘
```

**Điểm quan trọng**

- **Một chiều:** UI → ViewModel → Repository → Room. Không có chiều ngược nào UI tự đọc DB.
- **Room là nguồn duy nhất:** UI chỉ nhận `Flow` từ DAO, nên khi thêm/sửa/xoá thì mọi màn hình tự cập nhật.
- **DI thủ công:** `AppContainer` tạo DB, DAO, Repository đúng một lần (`by lazy`), không dùng Hilt để giữ build đơn giản với AGP 9.
- **Không lưu tổng:** calo/đạm/carb/béo của ngày, của bữa đều tính bằng `SUM(...)` trong SQL, tránh lệch dữ liệu.
- **Snapshot dinh dưỡng:** `meal_items` lưu bản sao calo/macro tại thời điểm thêm món, nên sửa danh mục `foods` sau này không làm sai lịch sử.

---

## 3. Cấu trúc thư mục

```
NutriVision/
├── app/src/main/
│   ├── assets/
│   │   ├── foods_nutrition.json      # 145 món + dinh dưỡng (seed DB)
│   │   ├── class_names_vi.json       # nhãn model (dùng cho tuần 4)
│   │   ├── model_meta.json           # metadata model (dùng cho tuần 4)
│   │   └── food_images/              # 145 ảnh WebP + CREDITS.json
│   ├── java/com/example/nutrivision/
│   │   ├── MainActivity.kt           # splash + điều hướng gốc (onboarding → auth/setup → shell)
│   │   ├── NutriVisionApp.kt         # Application, khởi tạo AppContainer
│   │   ├── di/
│   │   │   └── AppContainer.kt       # DI thủ công
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── NutriVisionDatabase.kt
│   │   │   │   ├── dao/              # UserDao, FoodDao, MealDao
│   │   │   │   ├── entity/           # UserEntity, FoodEntity, MealEntity, MealItemEntity
│   │   │   │   ├── relation/Relations.kt
│   │   │   │   └── seed/FoodSeeder.kt
│   │   │   ├── mapper/Mappers.kt
│   │   │   ├── prefs/                # SessionManager, ThemeManager, LanguageManager
│   │   │   └── repository/           # UserRepository, FoodRepository, MealRepository
│   │   ├── domain/
│   │   │   ├── calc/                 # NutritionCalculator, DateUtils
│   │   │   ├── model/Models.kt
│   │   │   └── text/                 # VietnameseText, AppLocale (tr Việt/Anh)
│   │   └── ui/
│   │       ├── components/           # DesignSystem, Charts, FoodVisuals, MonthCalendar
│   │       ├── navigation/AppTab.kt
│   │       ├── screens/              # 10 màn hình
│   │       ├── text/                 # LocalLanguageController
│   │       ├── theme/                # Color, Theme, Type
│   │       ├── util/                 # Formatters, SystemBars
│   │       └── viewmodel/            # 6 ViewModel
│   └── res/
├── app/src/test/java/com/example/nutrivision/
│   ├── NutritionCalculatorTest.kt
│   ├── VietnameseTextTest.kt
│   └── ExampleUnitTest.kt
├── docs/
│   ├── TUAN3_CO_SO_DU_LIEU.md        # tài liệu thiết kế
│   ├── BAO_CAO_TUAN3.md              # báo cáo này
│   └── screenshots/                  # ảnh kiểm chứng trên emulator
├── ai_tools/
│   ├── fetch_food_images.py          # tải ảnh món (HF + Commons + Wikipedia)
│   ├── make_contact_sheet.py         # xuất lưới ảnh để rà soát
│   └── _preview/                     # contact sheet (không đóng gói vào APK)
└── schemas/                          # Room export schema
```

---

## 4. Thiết kế cơ sở dữ liệu (4 bảng)

```
users 1 ─── N meals 1 ─── N meal_items N ─── 1 foods
```

| Bảng | Khoá chính | Trường chính | Ghi chú |
|---|---|---|---|
| `users` | `user_id` | email, tên, giới tính, tuổi, cao, nặng, vận động, goal, BMR, TDEE, target calo/P/C/F | 1 dòng / người dùng; demo `"local"` |
| `foods` | `id` (slug) | tên, category, kind, portion, calo/macro theo 100g, `search_key`, `is_custom`, `owner_user_id` | Seed 145 món từ assets |
| `meals` | `id` (UUID) | `user_id`, `meal_type`, `date`, photo, note, `sync_status` | `UNIQUE(user_id, date, meal_type)` |
| `meal_items` | `id` (UUID) | `meal_id`, `food_id`, `food_name`, gram, snapshot calo/macro/xơ/Na, source, confidence | `ON DELETE CASCADE` theo meal; soft delete |

**Quy ước**

- Ngày lưu dạng `"YYYY-MM-DD"` để so sánh và `GROUP BY` chính xác.
- Xoá món = set `deleted_at`, không xoá cứng → giữ lịch sử và chuẩn bị cho đồng bộ.
- `sync_status` để dành cho Firebase ở tuần 4–5.
- Không lưu tổng calo ngày/bữa trong bảng, luôn `SUM` từ `meal_items` đang hoạt động.

---

## 5. Quy tắc "chỉ dùng dữ liệu thật"

- Không có số calo/macro nào viết cứng trong màn hình.
- Chưa có dữ liệu → hiển thị `–` hoặc "Chưa có dữ liệu", không hiện số mẫu.
- Mục tiêu calo/macro lấy từ hồ sơ người dùng (Mifflin–St Jeor + hệ số vận động + mức goal).
- Tổng ngày, tổng bữa, calo trung bình, số ngày có ghi bữa đều tính từ DB.
- Khi xoá món, tổng calo/macro và thanh tiến trình cập nhật lại ngay do `Flow` từ Room.
- Placeholder tuần 4–5 chỉ nói "đang hoàn thiện", không mô phỏng kết quả AI.

---

## 6. Ảnh món ăn offline

**Yêu cầu:** mỗi món trong danh mục có một ảnh riêng, không phải tải tay từng ảnh, không phụ thuộc mạng khi dùng app.

### 6.1. Nguồn ảnh

| Nguồn | Số món | Cách lấy |
|---|---|---|
| Dataset huấn luyện `jeandeanny/vn-food-combined-2026` | 73 | HuggingFace **datasets-server** (`/statistics` + `/rows`), mỗi lớp lấy tối đa 3 ảnh |
| Wikimedia Commons (CC/PD) | 67 | API `generator=search`, lọc giấy phép và kích thước |
| Wikipedia (vi/en) | 5 | Ảnh lead của bài món ăn, truy vết giấy phép qua Commons |
| **Tổng** | **145** | Không có ảnh trùng (kiểm tra MD5) |

Không tải toàn bộ dataset 11 GB: script chỉ đọc thống kê để tính offset của từng lớp rồi tải đúng vài ảnh cần.

### 6.2. Quy trình tự động

1. `dataset_offsets()` đọc tần suất lớp → tính offset luỹ kế, lưu cache `ai_tools/_source/hf_offsets.json`.
2. `/rows?offset=N&length=3` lấy 3 ứng viên của lớp, chấm điểm nét (variance of edges) × diện tích, chọn ảnh tốt nhất.
3. 14 món không nằm trong 131 lớp model (chả giò, phở bò, phở gà, xôi gà, trà sữa, kimbap, udon, tteokbokki…) được tìm trên Commons/Wikipedia.
4. Ảnh được resize cạnh dài ≤ 720 px, nén **WebP quality 82** → `assets/food_images/<food_id>.webp`.
5. Chạy lại nhiều lần an toàn: ảnh đã có được bỏ qua (resume), gặp HTTP 429 thì chờ theo exponential backoff (8 → 256 giây), giãn cách 1,6 giây/request.

### 6.3. Hậu kiểm ảnh

- `make_contact_sheet.py` xuất 4 trang lưới ảnh kèm tên món + id để rà bằng mắt.
- Ảnh sai nhãn, ảnh bao bì, ảnh có watermark được thay bằng file Commons chọn tay trong `SPECIFIC_FILES` (`--specific --only <id>`).
- Một số ảnh phải đổi nguồn vì kết quả tìm kiếm tự động trả về ảnh không liên quan (ví dụ ảnh chân dung, ảnh biển hiệu, ảnh nguyên liệu).

### 6.4. Hiển thị trong app

- `FoodVisuals.kt`: `FoodAvatar` / `FoodIdAvatar` dùng Coil `SubcomposeAsyncImage` đọc `file:///android_asset/food_images/<id>.webp`.
- Ảnh hiện ở **Tìm món** (56 dp), **Chi tiết món** (76 dp) và **từng dòng món trong Nhật ký** (42 dp).
- Thiếu ảnh hoặc lỗi giải mã → tự động fallback gradient + icon theo nhóm món, không hiện ô trống.
- Toàn bộ ảnh nằm trong APK nên xem được offline, không cần quyền mạng.

### 6.5. Dung lượng và giấy phép

- 145 ảnh WebP: **7,87 MB** (trung bình ~54 KB/ảnh), APK debug ~25 MB.
- `assets/food_images/CREDITS.json` ghi từng món: nguồn, tên file, giấy phép, tác giả, trang gốc.
- Ảnh từ dataset Việt Nam: **CC BY-NC-SA 4.0** — chỉ dùng cho học tập/nghiên cứu, cần ghi nguồn, không dùng thương mại.
- Ảnh Commons/Wikipedia: CC0, CC BY, CC BY-SA hoặc Public Domain; giữ đúng điều kiện ghi công.

---

## 7. Trải nghiệm theo ngày, thống kê tháng, giao diện và ngôn ngữ

### 7.1. Ngày tự động

- `DateUtils.todayFlow()` phát `LocalDate` theo đồng hồ máy, kiểm tra mỗi 30 giây.
- `DiaryViewModel` chỉ nhảy sang ngày mới khi người dùng **đang xem hôm nay**; nếu đang xem một ngày cũ thì giữ nguyên để không mất ngữ cảnh.
- Sang ngày mới, trang chủ và nhật ký hiện 0 kcal, nhưng bữa ăn hôm qua vẫn nằm trong DB và mở lại được từ lịch.
- Nhãn thứ/ngày trong dải 7 ngày tính từ `LocalDate`, không viết cứng.

### 7.2. Lịch tháng và thống kê theo tháng

- Dưới dải 7 ngày có nút **"Xem lịch tháng"** mở hộp lịch lớn: thứ Hai là cột đầu, mũi tên lùi/tiến **theo tháng và theo năm**, ngày tương lai bị vô hiệu hoá, ngày có bữa ăn được đánh dấu.
- Chọn một ngày trong lịch → Nhật ký mở đúng ngày đó, dữ liệu đọc từ DB.
- Thống kê có hai phạm vi `StatsRange.WEEK` và `StatsRange.MONTH`. DAO dùng `BETWEEN :fromDate AND :toDate` trên `meals.date` nên cùng một truy vấn phục vụ cả tuần và tháng.
- Theo tháng, người dùng lùi/tiến từng tháng; số liệu gồm tổng calo, trung bình mỗi ngày có ghi, số ngày đã ghi, biểu đồ cột từng ngày và cơ cấu đạm/carb/béo. Ngày không ghi bữa tính là 0 kcal và được ghi rõ trong phần chú thích.

### 7.3. Sáng / tối

- Mặc định **theo hệ thống** (`ThemeMode.SYSTEM`); người dùng đổi Sáng/Tối trong Cá nhân, lựa chọn lưu ở `nutrivision_theme` nên mở lại app vẫn đúng.
- Màu là token trong `Color.kt`, đọc `ThemePaletteState.isDark` qua `pick(light, dark)`; đổi theme là đổi token, không sửa từng màn hình.
- Gradient giữ `ForestDeep` cố định để thẻ nổi bật vẫn đậm; chữ/icon trên nền mint dùng `ForestInk` để đủ tương phản ở cả hai chế độ.

### 7.4. Tiếng Việt / English

- Không dùng resource Android: toàn bộ chuỗi nằm trong Kotlin qua `tr("chuỗi Việt", "English")`. `AppLocale.language` là snapshot state nên đổi ngôn ngữ là cả cây Compose vẽ lại ngay, không cần khởi động lại Activity.
- Lựa chọn lưu ở `nutrivision_language`, mặc định **tiếng Việt**; hộp chọn trong Cá nhân hiện tên bản ngữ ("Tiếng Việt" / "English") để nhận ra cả khi đang ở ngôn ngữ kia.
- Định dạng số và ngày đổi theo ngôn ngữ: `1.731` / `1,731`; `09/11/2026` / `Nov 9, 2026`; thứ trong tuần, tên tháng, nhãn giới tính – vận động – mục tiêu – loại bữa – chế độ giao diện, thông báo lỗi và câu xác nhận xoá đều có cả hai bản.

---

## 8. Danh sách file

### 8.1. Thêm mới

| File | Vai trò |
|---|---|
| `data/local/NutriVisionDatabase.kt` | Room DB, 4 entity, callback seed |
| `data/local/dao/UserDao.kt` | Truy vấn hồ sơ |
| `data/local/dao/FoodDao.kt` | Tìm kiếm món, lọc theo loại |
| `data/local/dao/MealDao.kt` | CRUD bữa/món, `SUM` dinh dưỡng, `GROUP BY` ngày |
| `data/local/entity/UserEntity.kt` | Bảng `users` |
| `data/local/entity/FoodEntity.kt` | Bảng `foods` |
| `data/local/entity/MealEntity.kt` | Bảng `meals` |
| `data/local/entity/MealItemEntity.kt` | Bảng `meal_items` |
| `data/local/relation/Relations.kt` | `MealWithItems`, projection dinh dưỡng |
| `data/local/seed/FoodSeeder.kt` | Đọc JSON assets, nạp 145 món một lần |
| `data/mapper/Mappers.kt` | Entity ↔ Domain |
| `data/prefs/SessionManager.kt` | Cờ onboarding / đăng nhập |
| `data/prefs/ThemeManager.kt` | `ThemeMode` (hệ thống / sáng / tối) + lưu lựa chọn |
| `data/prefs/LanguageManager.kt` | `AppLanguage` (vi / en) + lưu lựa chọn |
| `data/repository/UserRepository.kt` | Hồ sơ + tính chỉ số |
| `data/repository/FoodRepository.kt` | Tìm món |
| `data/repository/MealRepository.kt` | CRUD trong transaction, soft delete |
| `di/AppContainer.kt` | DI thủ công |
| `domain/calc/NutritionCalculator.kt` | BMR, TDEE, mục tiêu, scale theo gram |
| `domain/calc/DateUtils.kt` | Khoá ngày, dải 7 ngày |
| `domain/model/Models.kt` | Model domain |
| `domain/text/VietnameseText.kt` | Bỏ dấu, chuẩn hoá `search_key` |
| `domain/text/AppLocale.kt` | Ngôn ngữ đang dùng + `tr("vi", "en")` |
| `ui/viewmodel/AppViewModel.kt` | Trạng thái phiên, hồ sơ |
| `ui/viewmodel/DashboardViewModel.kt` | Trang chủ |
| `ui/viewmodel/DiaryViewModel.kt` | Nhật ký theo ngày |
| `ui/viewmodel/SearchViewModel.kt` | Tìm kiếm + lọc |
| `ui/viewmodel/MealDetailViewModel.kt` | Thêm/sửa/xoá món |
| `ui/viewmodel/StatsViewModel.kt` | Thống kê |
| `ui/components/DesignSystem.kt` | Nút, card, thanh macro, empty state |
| `ui/components/Charts.kt` | Biểu đồ cột 7 ngày / theo tháng |
| `ui/components/MonthCalendar.kt` | Hộp lịch tháng/năm, thứ Hai đầu tuần |
| `ui/text/LanguageController.kt` | `LocalLanguageController` cho hộp chọn ngôn ngữ |
| `ui/components/FoodVisuals.kt` | Ảnh món (Coil) + fallback icon/màu theo nhóm món |
| `ui/navigation/AppTab.kt` | 5 tab + route overlay |
| `ui/screens/MainShell.kt` | Khung 5 tab, chuyển tab có animation |
| `ui/screens/DashboardScreen.kt` | Trang chủ |
| `ui/screens/DiaryScreen.kt` | Nhật ký |
| `ui/screens/SearchScreen.kt` | Tìm món |
| `ui/screens/MealDetailScreen.kt` | Chi tiết & chỉnh món |
| `ui/screens/StatsScreen.kt` | Thống kê |
| `ui/screens/ProfileScreen.kt` | Tab cá nhân |
| `ui/screens/CameraScreen.kt` | Placeholder nhận diện |
| `ui/screens/OnboardingScreen.kt` | 3 slide giới thiệu |
| `ui/screens/AuthScreen.kt` | Đăng nhập/đăng ký cục bộ |
| `ui/screens/ProfileSetupScreen.kt` | Thiết lập hồ sơ |
| `ui/util/Formatters.kt` | Định dạng số, kcal, ngày |
| `ui/util/SystemBars.kt` | Màu status bar theo tab |
| `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` | Design system |
| `assets/foods_nutrition.json` | 145 món kèm dinh dưỡng |
| `assets/food_images/*.webp` | 145 ảnh món, đóng gói offline (~7,9 MB) |
| `assets/food_images/CREDITS.json` | Nguồn, giấy phép, tác giả của từng ảnh |
| `ai_tools/fetch_food_images.py` | Tải ảnh từ HF datasets-server / Commons / Wikipedia, có throttle + resume |
| `ai_tools/make_contact_sheet.py` | Xuất lưới ảnh để rà soát thủ công |
| `docs/TUAN3_CO_SO_DU_LIEU.md` | Thiết kế trước khi code |
| `app/src/test/.../NutritionCalculatorTest.kt` | Test công thức |
| `app/src/test/.../VietnameseTextTest.kt` | Test tìm không dấu |

### 8.2. Sửa đổi

| File | Nội dung |
|---|---|
| `MainActivity.kt` | Splash, điều hướng gốc, khởi tạo ViewModel, cấp theme + ngôn ngữ cho cây Compose |
| `NutriVisionApp.kt` | Khởi tạo `AppContainer` |
| `di/AppContainer.kt` | Thêm `themeManager`, `languageManager` |
| `domain/calc/DateUtils.kt` | `todayFlow()`, lịch tháng, nhãn thứ/tháng/ngày theo ngôn ngữ |
| `domain/model/Models.kt` | Nhãn enum song ngữ (`MealType`, `ActivityLevel`, `Goal`), `bmiLabel` |
| `ui/theme/Color.kt`, `Theme.kt` | Bảng màu sáng/tối theo token, `ForestInk` cho chữ trên nền mint |
| `ui/screens/ProfileScreen.kt` | Hàng "Giao diện" + "Ngôn ngữ", hai hộp chọn, chuỗi song ngữ |
| `ui/screens/DiaryScreen.kt` | Dải 7 ngày tự cuộn theo ngày, nút mở lịch tháng, chuỗi song ngữ |
| `ui/screens/StatsScreen.kt` | Chuyển 7 ngày / theo tháng, lùi tiến tháng, chuỗi song ngữ |
| `ui/viewmodel/StatsViewModel.kt` | `StatsRange`, truy vấn `BETWEEN`, tự sang tháng mới |
| `ui/screens/MealDetailScreen.kt` | Hàng chọn bữa cuộn ngang (nhãn tiếng Anh dài hơn), "Sodium" |
| `ui/util/Formatters.kt` | `1.731` / `1,731`, lời chào theo giờ và ngôn ngữ |
| `app/build.gradle.kts` | Room, KSP, Compose, lifecycle, Coil 2.7.0 |
| `gradle/libs.versions.toml` | Phiên bản Room 2.8.5, KSP, Compose BOM, Coil |
| `gradle.properties` | `android.disallowKotlinSourceSets=false` cho KSP |
| `AndroidManifest.xml` | Application class, theme |

### 8.3. Xoá

| File | Lý do |
|---|---|
| `ui/screens/RecognitionResultScreen.kt` | Màn giả lập kết quả AI không còn dùng; tuần 4 sẽ làm thật |
| Route `RecognitionResult` trong `AppTab.kt` | Không còn màn tương ứng |

---

## 9. Kiểm thử

### 9.1. Unit test

```
.\gradlew.bat :app:testDebugUnitTest
BUILD SUCCESSFUL — 14 test pass
```

| Test | Nội dung |
|---|---|
| `NutritionCalculatorTest` | BMR, TDEE, mục tiêu calo, macro, scale theo gram, BMI |
| `VietnameseTextTest` | Bỏ dấu tiếng Việt, chuẩn hoá `search_key`, đoán `kind` |

### 9.2. Kiểm thử trên emulator (Pixel 7, Android 15)

| Bước | Kết quả |
|---|---|
| Onboarding 3 slide | Hiển thị đúng, chuyển slide có animation, "Bỏ qua"/"Bắt đầu" hoạt động |
| Đăng nhập | Vào được app, phiên lưu lại |
| Thiết lập hồ sơ | Lưu thành công, tính BMR 1698 / TDEE 2037 / mục tiêu 1731 kcal |
| Trang chủ | Hiện mục tiêu 1731 kcal, còn lại 1731 khi chưa có món hôm nay |
| Ngày tự động | Đổi ngày trên máy → trang chủ và nhật ký về 0 kcal sau ≤30 giây, bữa cũ vẫn còn trong lịch sử |
| Ghi bữa 09/11 | Bánh mì 200 g → 460 kcal, macro 21 / 48 / 19 |
| Lịch tháng | Mở từ Nhật ký, đúng "Tháng 11, 2026", chọn 09/11 mở đúng ngày có bữa, ngày tương lai bị mờ |
| Thống kê 7 ngày | 1/7 ngày có ghi, tổng 460 kcal, trung bình 460 kcal/ngày có ghi |
| Thống kê tháng | 1/30 ngày, tổng tháng 460 kcal, lùi/tiến tháng hoạt động |
| Giao diện | Chọn Sáng/Tối đổi ngay; để "Theo hệ thống" thì đổi theme máy, app đổi theo; mở lại app vẫn đúng |
| Ngôn ngữ | Chọn English → toàn bộ nhãn, số `1,731`, ngày `Nov 11, 2026` đổi ngay; chọn Tiếng Việt → về `1.731`, `11/11/2026` |
| Tắt app mở lại | Dữ liệu, giao diện và ngôn ngữ còn nguyên |

### 9.3. Kiểm chứng trực tiếp database

Kéo `databases/nutrivision.db` + `-wal` từ thiết bị và truy vấn bằng SQLite:

| Kiểm tra | Kết quả |
|---|---|
| Số bản ghi `foods` | 145 |
| Số bản ghi `users` | 1 (`local`, "Minh", BMR 1698, TDEE 2037, target 1731) |
| Bữa đã lưu | `2026-11-09`, Bánh mì 200 g, 460 kcal, 21 / 48 / 19 |
| Tổng calo ngày | Khớp với UI và với thống kê tuần/tháng |
| `shared_prefs/nutrivision_language.xml` | `vi` hoặc `en`, đổi đúng theo lựa chọn |
| `shared_prefs/nutrivision_theme.xml` | `system` / `light` / `dark`, giữ sau khi tắt app |

Ảnh kiểm chứng nằm trong `docs/screenshots/`:
`01_onboarding` … `24_diary_week_strip` (CRUD, ảnh món, dải 7 ngày),
`25_calendar_month_vi`, `26_stats_month_vi`, `27_profile_dark_vi`, `28_dashboard_dark_vi` (lịch tháng, thống kê tháng, nền tối),
`29_dashboard_en`, `30_calendar_month_en`, `31_stats_month_en`, `32_meal_detail_en` (giao diện tiếng Anh).

---

## 10. Hạn chế và hướng tuần 4–5

| Hạng mục | Hiện tại | Tuần sau |
|---|---|---|
| Nhận diện ảnh | Placeholder trung thực | CameraX + TFLite (MobileNetV3-Large, top-1 76.50 %, top-3 89.79 %, threshold 0.70) |
| Gợi ý món bằng AI | Chưa có | Gemini API gọi qua backend, **không nhúng API key vào APK** |
| Đăng nhập | Cục bộ theo thiết bị | Firebase Auth |
| Đồng bộ | Chỉ trên máy | Firebase Firestore, dùng `sync_status` và soft delete đã chuẩn bị |
| Model AI | Không huấn luyện lại | Giữ nguyên model đã huấn luyện |

**Nguyên tắc giữ nguyên:** model không được huấn luyện lại trong app; API key không nằm trong APK; mọi số liệu hiển thị vẫn lấy từ DB, không quay lại số cứng.
