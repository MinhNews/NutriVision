# BÁO CÁO TUẦN 2 — THIẾT KẾ GIAO DIỆN

**Môn học:** Lập trình di động  
**Lớp:** .................... **Nhóm:** .................... (3 thành viên)  
**Giảng viên hướng dẫn:** Lê Minh Trí  
**Tên đề tài:** NutriVision – Ứng dụng phân tích dinh dưỡng món ăn từ hình ảnh  
**Ngày báo cáo:** ....../....../2026  

| STT | Họ và tên | MSSV | Vai trò | Công việc chính tuần 2 |
|---|---|---|---|---|
| 1 | .............................. | .............. | Nhóm trưởng – Android Lead | Xây dựng luồng di chuyển, cấu hình theme và dựng layout Compose |
| 2 | .............................. | .............. | Data & Backend Engineer | Thiết kế bảng màu Macro, chuẩn bị danh mục thành phần/topping |
| 3 | .............................. | .............. | UI/UX & QA Lead | Thiết kế Design System, vẽ Wireframe và Mockup Figma 9 màn hình |

---

## BẢNG ĐỐI CHIẾU YÊU CẦU TUẦN 2 CỦA GIẢNG VIÊN

| Yêu cầu của đề bài | Nội dung thực hiện trong báo cáo | Trạng thái |
|---|---|:---:|
| **1. Xây dựng luồng di chuyển giữa các màn hình** | Sơ đồ Screen Flow và Navigation Graph chi tiết (Mục 1) | **Hoàn thành** |
| **2. Thống nhất bảng màu, font chữ, icon** | Bộ Design System: Bảng màu Material 3, Macro colors, Font Inter, Icon Material Symbols (Mục 2) | **Hoàn thành** |
| **3. Thiết kế wireframe và mockup trên Figma** | Bộ đặc tả 9 màn hình chính từ Splash, Dashboard đến Customizer (Mục 3 + Link Figma) | **Hoàn thành** |
| **4. Dựng layout tĩnh cho các màn hình trong project** | Cấu trúc code Jetpack Compose, theme và code mẫu màn hình tĩnh (Mục 4) | **Hoàn thành** |

---

# 1. XÂY DỰNG LUỒNG DI CHUYỂN GIỮA CÁC MÀN HÌNH (SCREEN FLOW)

Nhóm đã xây dựng luồng di chuyển tổng thể cho ứng dụng, đảm bảo tối ưu hóa số bước thao tác cho người dùng:

```text
[Splash Screen] ──► [Kiểm tra đăng nhập]
                          │
          ┌───────────────┴──────────────┐
     (Chưa đăng nhập)               (Đã đăng nhập)
          ▼                              ▼
  [Đăng nhập / Đăng ký]          [DASHBOARD - Trang chủ]
          ▼                              │
  [Thiết lập Hồ sơ cá nhân]               ├─► [Xem chi tiết Bữa ăn]
  (Chiều cao, cân nặng, TDEE)             ├─► [Tìm kiếm món thủ công]
          │                              │
          └──────────────────────────────┘
                                         │ (Bấm nút Camera giữa thanh điều hướng)
                                         ▼
                                  [MÀN HÌNH CAMERA]
                                  (Chụp ảnh / Chọn từ Thư viện)
                                         │
                                         ▼
                                  [AI & TINH CHỈNH KHẨU PHẦN]
                                  • Nhận diện món (TFLite Offline / Gemini Online)
                                  • Chỉnh khẩu phần (Tô nhỏ/vừa/lớn, số gram)
                                  • Thêm/bớt topping (+thịt, +trứng, -bánh)
                                  • Tính calo, đạm, carb, béo thời gian thực
                                         │
                                         ▼ (Bấm Lưu)
                                  [CẬP NHẬT DASHBOARD & NHẬT KÝ]
```

---

# 2. HỆ THỐNG THIẾT KẾ: BẢNG MÀU, FONT CHỮ & ICON

### 2.1. Thống nhất Bảng màu (Color Palette)
Ứng dụng theo đuổi phong cách tươi sáng, hiện đại và chuẩn Material 3:

* **Màu chủ đạo (Primary):** `#10B981` (Emerald Green) – tượng trưng cho sự lành mạnh, sức khỏe và thực phẩm tươi sống.
* **Màu phụ (Secondary):** `#F59E0B` (Amber Orange) – tượng trưng cho năng lượng và calo.
* **Màu công nghệ (Accent):** `#6366F1` (Indigo Purple) – dùng riêng cho các tính năng AI thông minh.
* **Quy chuẩn màu 3 chất dinh dưỡng vĩ mô (Macro Colors):**
  * **Protein (Chất đạm):** `#3B82F6` (Xanh dương)
  * **Carb (Tinh bột):** `#F59E0B` (Vàng cam)
  * **Fat (Chất béo):** `#EF4444` (Đỏ)
* **Màu nền (Background):** `#F8FAFC` (Light Mode) và `#0F172A` (Dark Mode).

### 2.2. Thống nhất Font chữ (Typography)
* **Font chữ:** **`Plus Jakarta Sans`** / **`Inter`** (hỗ trợ hiển thị tiếng Việt hoàn hảo, số hiển thị rõ ràng).
* **Cấp bậc kiểu chữ:**
  * **Display Large (32sp, Bold):** Số Calo lớn ở Dashboard (`1,250`).
  * **Headline Medium (22sp, SemiBold):** Tên món ăn, tiêu đề màn hình (`Phở bò tái`).
  * **Title Medium (16sp, SemiBold):** Tên bữa ăn, tiêu đề thẻ.
  * **Body Medium (14sp, Regular):** Nội dung mô tả, số gram, thành phần.
  * **Label Large (14sp, SemiBold):** Chữ trên nút bấm hành động.

### 2.3. Thống nhất Bộ Icon
* **Bộ icon sử dụng:** **Material Symbols Rounded** của Google (nét bo tròn mềm mại, kích thước chuẩn 24×24 dp).
* Đồng bộ nét vẽ trên toàn bộ thanh điều hướng, các thẻ bữa ăn và công cụ camera.

---

# 3. BỘ WIREFRAME & MOCKUP TRÊN FIGMA (9 MÀN HÌNH CHÍNH)

Nhóm đã hoàn thiện bản thiết kế Wireframe và Mockup với **9 màn hình chính**:

1. **Screen 01 – Chào mừng & Onboarding:** Giới thiệu 3 giá trị cốt lõi: Quét AI nhanh – Món ăn Việt – Dinh dưỡng khoa học.
2. **Screen 02 – Đăng nhập & Đăng ký:** Form nhập liệu email, mật khẩu với giao diện trực quan, rõ ràng.
3. **Screen 03 – Khởi tạo Hồ sơ cá nhân:** Chọn giới tính, tuổi, kéo thước đo chiều cao, cân nặng, tự động tính chỉ số BMI và TDEE.
4. **Screen 04 – Dashboard (Trang chủ):** Vòng tròn tiến độ calo lớn ở trung tâm, 3 thanh đo Macro, danh sách 4 thẻ bữa ăn (Sáng, Trưa, Tối, Phụ) và nút Camera nổi bật ở giữa Bottom Bar.
5. **Screen 05 – Camera Quét món ăn:** Khung ngắm chụp căn giữa, nút bật đèn flash, nút chọn ảnh từ thư viện máy.
6. **Screen 06 – Màn hình AI & Tinh chỉnh Khẩu phần (Signature Feature):**
   * Hiển thị ảnh chụp + Thẻ kết quả AI (kèm độ tin cậy và 2 món phụ dự phòng).
   * Bộ chọn khẩu phần nhanh (Nhỏ/Vừa/Lớn) hoặc gõ số gram cụ thể.
   * Dãy nút chip thêm/bớt topping (+ thịt, + trứng, - bánh).
   * Bảng hiển thị Calo, Đạm, Carb, Béo tự động nhảy số theo thời gian thực.
7. **Screen 07 – Tìm kiếm món ăn thủ công:** Tra cứu danh mục món Việt và Món ngoại khi người dùng không chụp ảnh.
8. **Screen 08 – Thống kê & Biểu đồ:** Biểu đồ cột 7 ngày so với vạch calo mục tiêu và biểu đồ tròn tỷ lệ Macro.
9. **Screen 09 – Hồ sơ cá nhân & Cài đặt:** Quản lý thông tin cá nhân, bật chế độ tối (Dark Mode), đăng xuất.

*(Link Figma của nhóm: [Đính kèm liên kết dự án Figma tại đây])*

---

# 4. KẾT QUẢ DỰNG LAYOUT TĨNH TRONG PROJECT ANDROID

Nhóm đã khởi tạo project Android Studio sử dụng **Jetpack Compose + Material 3** và triển khai xong mã nguồn layout tĩnh:

1. **Khởi tạo hệ thống Theme (`ui/theme/`):**
   * File `Color.kt`: Khai báo toàn bộ mã màu thương hiệu và màu Macro chuẩn.
   * File `Theme.kt`: Cấu hình `NutriVisionTheme` hỗ trợ tự động thích ứng giao diện.
2. **Dựng các màn hình tĩnh cốt lõi:**
   * `DashboardScreen.kt`: Dựng hoàn chỉnh Header, Card vòng tròn Calo, 3 thanh Macro và 4 thẻ bữa ăn tĩnh bằng `@Composable`.
   * `MealCustomizerScreen.kt`: Dựng thẻ kết quả AI, các nút FilterChip chọn khẩu phần, AssistChip chọn topping và Card tính toán dinh dưỡng tĩnh.
3. **Kiểm tra giao diện:**
   * Sử dụng tính năng `@Preview` của Jetpack Compose để kiểm tra giao diện trực tiếp trên nhiều kích thước màn hình Android khác nhau.

---

# 5. KẾ HOẠCH TUẦN 3 TIẾP THEO

* Thiết kế sơ đồ quan hệ thực thể (ERD) hoàn chỉnh gồm 5 bảng: `users`, `foods`, `toppings`, `meals`, `meal_items`.
* Cài đặt thư viện **Room (SQLite)** và tạo sẵn bộ nạp dữ liệu mẫu (Seed Data) với danh mục món Việt & món ngoại.
* Tích hợp kiến trúc **MVVM (Model - View - ViewModel) + Repository** để chuẩn bị gắn dữ liệu vào các màn hình tĩnh đã dựng ở tuần 2.

