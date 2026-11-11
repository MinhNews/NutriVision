# HỆ THỐNG THIẾT KẾ (DESIGN SYSTEM) & UI GUIDE — NUTRIVISION

> **Mục tiêu:** Thống nhất toàn bộ bảng màu, font chữ, icon và quy chuẩn giao diện (UI) để thành viên vẽ Figma (TV3) và thành viên dựng code layout (TV1, TV2) làm việc ăn khớp 100%.

---

## 1. Triết lý thiết kế (Design Philosophy)
* **Phong cách:** Hiện đại, tối giản, chuẩn **Material 3 (Material You)** của Google.
* **Cảm xúc mang lại:** Tươi mới, lành mạnh, công nghệ thông minh, đáng tin cậy và không tạo áp lực ăn kiêng tiêu cực.
* **Khả năng tương thích:** Hỗ trợ tối ưu cả **Light Mode** (mặc định) và **Dark Mode**.

---

## 2. Bảng màu chuẩn (Color Palette)

### 2.1. Màu thương hiệu & Nhận diện (Brand Colors)

| Tên màu | Mã HEX (Light) | Mã HEX (Dark) | Ứng dụng thực tế |
|---|---|---|---|
| **Primary (Xanh Dinh Dưỡng)** | `#10B981` (Emerald 500) | `#34D399` (Emerald 400) | Màu chủ đạo: Nút bấm chính, viền active, vòng tròn calo tiến độ, icon tab đang chọn. |
| **Primary Container** | `#D1FAE5` (Emerald 100) | `#064E3B` (Emerald 900) | Nền chip được chọn, nền card nổi bật. |
| **Secondary (Cam Năng Lượng)** | `#F59E0B` (Amber 500) | `#FBBF24` (Amber 400) | Màu phụ: Calo tiêu thụ, cảnh báo nhẹ, huy hiệu streak. |
| **Accent (Tím AI Thông Minh)** | `#6366F1` (Indigo 500) | `#818CF8` (Indigo 400) | Màu cho tính năng AI: Tag "AI nhận diện", nút "Quét bằng Cloud AI". |

### 2.2. Bảng màu 3 chất dinh dưỡng vĩ mô (Macro Colors)
*Quy chuẩn màu này được dùng cố định ở mọi màn hình (Dashboard, Chi tiết món, Biểu đồ) để người dùng nhìn là nhận ra ngay:*

| Chỉ số | Mã HEX | Tên màu | Ý nghĩa |
|---|---|---|---|
| **Protein (Đạm)** | `#3B82F6` | Blue 500 | Xây dựng cơ bắp, săn chắc |
| **Carbs (Tinh bột)** | `#F59E0B` | Amber 500 | Năng lượng hoạt động chính |
| **Fat (Chất béo)** | `#EF4444` | Red 500 | Chất béo cần kiểm soát |
| **Fiber (Chất xơ)** | `#10B981` | Emerald 500 | Tốt cho tiêu hóa |
| **Sodium (Muối/Natri)** | `#8B5CF6` | Purple 500 | Cảnh báo khi vượt ngưỡng WHO (>2000mg) |

### 2.3. Bảng màu nền & Trung tính (Neutral Colors)

| Thành phần | Light Mode | Dark Mode | Ghi chú |
|---|---|---|---|
| **Nền ứng dụng (Background)** | `#F8FAFC` (Slate 50) | `#0F172A` (Slate 900) | Nền chính của toàn app |
| **Nền thẻ (Surface / Card)** | `#FFFFFF` (White) | `#1E293B` (Slate 800) | Nền các thẻ bữa ăn, card món ăn |
| **Viền mỏng (Border / Divider)** | `#E2E8F0` (Slate 200) | `#334155` (Slate 700) | Đường phân cách, viền ô nhập |
| **Văn bản chính (Text Primary)** | `#0F172A` (Slate 900) | `#F8FAFC` (Slate 50) | Tiêu đề, số calo lớn |
| **Văn bản phụ (Text Secondary)** | `#64748B` (Slate 500) | `#94A3B8` (Slate 400) | Đơn vị gram, ngày tháng, mô tả |
| **Trạng thái Thành công** | `#10B981` | `#34D399` | Đạt mục tiêu, lưu thành công |
| **Trạng thái Cảnh báo/Lỗi** | `#EF4444` | `#F87171` | Vượt calo mục tiêu, nhập sai pass |

---

## 3. Hệ thống kiểu chữ (Typography)

* **Font chữ đề xuất:** **`Inter`** hoặc **`Plus Jakarta Sans`** (Có sẵn miễn phí trên Google Fonts, hỗ trợ tiếng Việt đầy đủ dấu, số hiển thị cực rõ nét). Trong Android Studio dùng font mặc định `Roboto` hoặc nhúng file `.ttf` của `Plus Jakarta Sans`.

### Cấp bậc chữ (Type Scale)

| Cấp bậc | Cỡ chữ (sp) | Độ đậm (Weight) | Ứng dụng |
|---|---|---|---|
| **Display Large** | 32 sp | Bold (700) | Con số Calo lớn ở Dashboard: `1,450` |
| **Headline Medium** | 22 sp | SemiBold (600) | Tiêu đề màn hình: *"Nhật ký bữa ăn"*, *"Hồ sơ"* |
| **Title Large** | 18 sp | SemiBold (600) | Tên món ăn: *"Phở bò tái nạm"*, Tiêu đề card |
| **Body Large** | 16 sp | Regular (400) | Chữ nội dung trong ô nhập, mô tả |
| **Body Medium** | 14 sp | Regular (400) | Tên topping, chi tiết dinh dưỡng |
| **Label Large** | 14 sp | SemiBold (600) | Chữ trên nút bấm: *"Lưu vào Bữa Trưa"* |
| **Caption / Small** | 12 sp | Medium (500) | Nhãn nhỏ: `kcal`, `protein`, giờ chụp |

---

## 4. Hệ thống Icon (Iconography)

* **Bộ Icon thống nhất:** **Material Symbols (Rounded)** của Google hoặc **Lucide Icons**.
* **Đặc điểm:** Bo góc tròn nhẹ, nét vẽ 2px đồng nhất, kích thước chuẩn 24×24 dp.

### Danh mục Icon các màn hình chính:
* **Bottom Navigation:**
  * Trang chủ: `home_rounded` (Ngôi nhà)
  * Nhật ký / Lịch: `calendar_today_rounded` (Cuốn lịch)
  * Nút Quét / Camera (ở giữa): `photo_camera_rounded` (Máy ảnh viền nổi)
  * Thống kê: `bar_chart_rounded` (Biểu đồ cột)
  * Cá nhân: `person_rounded` (Hồ sơ)
* **Icon bữa ăn:**
  * Bữa Sáng: `wb_sunny_rounded` (Mặt trời mọc)
  * Bữa Trưa: `restaurant_rounded` (Dao nĩa)
  * Bữa Tối: `nights_stay_rounded` (Trăng lưỡi liềm)
  * Ăn Vặt: `local_cafe_rounded` (Cốc cà phê / trái táo)
* **Icon tương tác:** `add_rounded` (+), `remove_rounded` (-), `search_rounded` (Kính lúp), `check_circle_rounded` (Dấu tick), `arrow_back_rounded` (Quay lại).

---

## 5. Quy chuẩn Component (UI Components)

* **Bo góc (Corner Radius):**
  * Nút bấm (Button): `24dp` (Bo tròn dạng viên thuốc - Pill button).
  * Thẻ nội dung (Card): `16dp` (Bo mềm mại hiện đại).
  * Ô nhập liệu (TextField): `12dp`.
  * Hộp thoại (Dialog / Bottom Sheet): `24dp` góc trên.
* **Đổ bóng (Elevation / Shadow):**
  * Hạn chế đổ bóng đen gắt. Dùng bóng mờ nhẹ tự nhiên: `box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05)`.
* **Thanh điều hướng dưới (Bottom Bar):**
  * Có 5 vị trí: 4 tab chính + 1 nút Camera nổi bật ở giữa với nền màu Primary (`#10B981`) lớn hơn để kích thích người dùng bấm chụp.

