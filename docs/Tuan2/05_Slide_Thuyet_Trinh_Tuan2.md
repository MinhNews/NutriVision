# SLIDE THUYẾT TRÌNH BÁO CÁO TIẾN ĐỘ TUẦN 2
## ĐỀ TÀI: NUTRIVISION — NHẬT KÝ DINH DƯỠNG & PHÂN TÍCH MÓN ĂN THÔNG MINH
**Môn học:** Lập trình di động  
**Giảng viên hướng dẫn:** ThS. Lê Minh Trí  
**Thời lượng trình bày:** 5 - 7 phút (Gồm 8 slide cô đọng, súc tích)

---

## SLIDE 1: TRANG TIÊU ĐỀ (TITLE SLIDE)
* **Tiêu đề lớn:** NUTRIVISION — HỆ THỐNG THEO DÕI DINH DƯỠNG THÔNG MINH
* **Tiêu đề phụ:** Báo Cáo Tiến Độ Tuần 2: Thiết Kế Giao Diện & Dựng Layout Tĩnh
* **Môn học:** Lập trình di động (Android với Jetpack Compose)
* **Giảng viên hướng dẫn:** ThS. Lê Minh Trí
* **Nhóm thực hiện (3 thành viên):**
  * Thành viên 1: [Họ và tên - MSSV] — Trưởng nhóm, Phụ trách Theme & Dashboard
  * Thành viên 2: [Họ và tên - MSSV] — Phụ trách Camera & Chi tiết món ăn
  * Thành viên 3: [Họ và tên - MSSV] — Phụ trách Auth, Profile & Điều hướng
* **Repo GitHub:** `https://github.com/MinhNews/NutriVision`

---

## SLIDE 2: MỤC TIÊU & TỔNG QUAN KẾT QUẢ TUẦN 2
* **Mục tiêu tuần 2 theo đề cương môn học:**
  1. Xây dựng luồng di chuyển giữa các màn hình (Screen Flow & Navigation).
  2. Thiết kế Wireframe và Mockup hoàn chỉnh trên Figma.
  3. Thống nhất Design System: Bảng màu thương hiệu, Typography, Iconography.
  4. Dựng bộ khung Layout tĩnh bằng Jetpack Compose trong Android Studio và quản lý Git.
* **Kết quả đạt được:**
  * ✅ Hoàn thành 14 màn hình Figma (cả dạng Mockup màu sắc và Wireframe khung xương).
  * ✅ Thống nhất Design System chuẩn Material 3.
  * ✅ Dựng thành công 5 màn hình tĩnh trên Android Studio, kết nối qua thanh Bottom Navigation.
  * ✅ Phân chia 3 nhánh Git độc lập, chuẩn quy trình làm việc nhóm.

---

## SLIDE 3: LUỒNG DI CHUYỂN MÀN HÌNH (SCREEN FLOW & NAVIGATION)
* **Nội dung trình bày:**
  * **Khối Xác thực (Auth Flow):** Chào mừng ➔ Đăng nhập / Đăng ký ➔ Thiết lập hồ sơ thể trạng (BMI/TDEE).
  * **Khối Trung tâm (Main Hub - Bottom Bar):**
    * Tab 1: **Trang chủ (Dashboard)** — Xem Calo còn lại, thanh tỷ lệ Đạm/Carb/Béo, 4 bữa ăn trong ngày.
    * Tab 2: **Quét món ăn (Camera)** — Khung ngắm chụp ảnh trực tiếp món ăn.
    * Tab 3: **Chi tiết món (Meal Detail)** — Tinh chỉnh khẩu phần, thêm topping, tính toán dinh dưỡng.
    * Tab 4: **Hồ sơ cá nhân (Profile)** — Cập nhật cân nặng, chiều cao, mục tiêu calo, đăng xuất.
  * **Khối Phụ trợ:** Nhật ký theo ngày, Tìm kiếm món ăn thủ công, Biểu đồ thống kê 7 ngày.
* *(Gợi ý hình ảnh đưa vào slide: Ảnh chụp sơ đồ Screen Flow từ Figma Page 03)*

---

## SLIDE 4: HỆ THỐNG THIẾT KẾ (DESIGN SYSTEM)
* **Bảng màu nhận diện thương hiệu:**
  * **Primary Green (`#10B981`):** Xanh tươi mát — biểu trưng cho lối sống lành mạnh, năng lượng sạch.
  * **Secondary Amber (`#F59E0B`):** Vàng cam — đại diện cho chỉ số năng lượng Calo.
  * **Accent Indigo (`#6366F1`):** Tím hiện đại — nhấn mạnh tính năng AI phân tích món ăn.
  * **Màu Macro 3 chất:** Xanh dương (Đạm) • Vàng cam (Carb) • Đỏ (Chất béo).
* **Typography & Iconography:**
  * Phông chữ: Chuẩn Material 3 Typography hiện đại, rõ ràng, dễ đọc khi đang nấu ăn hoặc đi ngoài trời.
  * Bộ icon: Tối giản, nét vẽ đồng nhất 2px, trực quan cho sức khỏe.

---

## SLIDE 5: SẢN PHẨM THIẾT KẾ FIGMA (WIREFRAME & MOCKUP)
* **Điểm nổi bật của bản thiết kế:**
  * Thiết kế theo chuẩn tỉ lệ màn hình Android hiện đại (390 x 844 dp).
  * Có đầy đủ cả 2 phiên bản:
    * **Wireframe (Trắng đen/Xám):** Tập trung vào cấu trúc thông tin, khoảng cách bố cục (Padding/Margin) và trải nghiệm UX.
    * **Mockup (Màu sắc hoàn thiện):** Thể hiện trực quan giao diện thực tế khi ứng dụng chạy.
* **Các màn hình cốt lõi đã thiết kế:**
  1. Màn hình Đăng nhập / Đăng ký (Auth).
  2. Màn hình Dashboard (Trang chủ theo dõi Calo trong ngày).
  3. Màn hình Camera chụp món ăn có khung ngắm (Viewfinder).
  4. Màn hình Tinh chỉnh khẩu phần & thêm bớt Topping.
  5. Màn hình Hồ sơ cá nhân BMI & TDEE.
* *(Gợi ý hình ảnh đưa vào slide: Ghép 4 mockup màn hình chính từ Figma)*

---

## SLIDE 6: DỰNG LAYOUT TĨNH TRÊN ANDROID STUDIO (JETPACK COMPOSE)
* **Công nghệ áp dụng:**
  * 100% Jetpack Compose với bộ thư viện Material 3 mới nhất.
  * Kiến trúc giao diện khai báo (Declarative UI): Chia nhỏ thành các Composable tái sử dụng.
* **Các Composable đã hoàn thành:**
  * `DashboardScreen`: Card Calo trung tâm, hàng tỷ lệ Macro, danh sách cuộn `LazyColumn` các bữa ăn.
  * `CameraScreen`: Bố cục Box toàn màn hình, khung ngắm viền bo góc, cụm nút chụp tròn.
  * `MealDetailScreen`: Thẻ hiển thị AI, FilterChip chọn khẩu phần (Nhỏ/Vừa/Lớn), AssistChip tùy chọn topping.
  * `AuthScreen` & `ProfileScreen`: Form nhập liệu `OutlinedTextField`, nút bấm bo tròn hiện đại.
* **Liên kết điều hướng tạm thời:** Tích hợp `NavigationBar` đáy để chuyển đổi mượt mà giữa các màn hình ngay trên máy thật / máy ảo.

---

## SLIDE 7: PHÂN CÔNG NHÓM & QUẢN LÝ MÃ NGUỒN GIT
* **Bảng phân chia công việc minh bạch:**
  * **Thành viên 1:** Cấu hình Theme (`Color.kt`, `Theme.kt`), Dựng layout `DashboardScreen.kt` ➔ Nhánh `feature/tv1-theme-and-dashboard`.
  * **Thành viên 2:** Dựng layout `CameraScreen.kt` và `MealDetailScreen.kt` ➔ Nhánh `feature/tv2-camera-and-meal-detail`.
  * **Thành viên 3:** Dựng layout `AuthScreen.kt`, `ProfileScreen.kt` và nối Bottom Navigation `MainActivity.kt` ➔ Nhánh `feature/tv3-auth-profile-navigation`.
* **Quy trình Git:** Mỗi bạn làm việc trên branch riêng, kiểm tra độc lập qua `@Preview`, tạo Pull Request và review chéo trước khi merge vào `main`.
* **Minh chứng:** Repo GitHub `MinhNews/NutriVision` thể hiện rõ lịch sử commit của các thành viên.

---

## SLIDE 8: KẾ HOẠCH TUẦN 3 & HỎI ĐÁP (NEXT STEPS & Q&A)
* **Kế hoạch Tuần 3 (Cơ sở dữ liệu & Kiến trúc):**
  1. Thiết kế mô hình dữ liệu quan hệ (ERD): Bảng `User`, `Meal`, `FoodItem`, `NutritionGoal`.
  2. Cài đặt Room Database (SQLite) cục bộ trên Android.
  3. Xây dựng cấu trúc thư mục theo kiến trúc chuẩn MVVM (Model - View - ViewModel).
  4. Thực hiện các thao tác Thêm - Sửa - Xóa (CRUD) dữ liệu bữa ăn cơ bản.
* **Chuẩn bị phần AI:**
  * Đăng ký Google Gemini API Key & khảo sát mô hình TFLite on-device nhận diện món ăn Việt Nam.
* **Phần giải đáp câu hỏi của Thầy và các bạn trong lớp.**

