# NutriVision · Figma Builder (Tuần 2)

Plugin Figma tạo **toàn bộ file thiết kế Tuần 2** bằng code: 4 trang, 28 frame (14 màn × wireframe + mockup), Design System và Screen Flow. Không cần vẽ tay từng khung.

> ⚠️ **Bắt buộc dùng Figma Desktop.** Figma bản web **không có** menu `Plugins → Development → Import plugin from manifest`, nên không nạp được plugin dev. Desktop miễn phí, đăng nhập cùng tài khoản; file tạo ra **tự đồng bộ lên Figma web** để xem/chia sẻ.

---

## 1. Cài Figma Desktop (1 lần)

1. Mở https://www.figma.com/downloads/ → tải **Figma for Windows**.
2. Cài đặt, mở app, đăng nhập bằng tài khoản Figma đang dùng trên web.
3. Tạo file mới: **Drafts → + New design file**, đổi tên ví dụ `NutriVision_W2`.

## 2. Nạp plugin (1 lần cho mỗi máy)

1. Trong Figma Desktop, mở file vừa tạo.
2. Menu trên thanh công cụ: **Plugins → Development → Import plugin from manifest…**
3. Chọn tệp:
   `E:\LTDD\Tuan2\figma-plugin\manifest.json`
4. Figma báo *"Plugin imported"*. Plugin xuất hiện trong **Plugins → Development** với tên **NutriVision Builder — Tuần 2**.

> Nếu không thấy mục **Development**: cập nhật Figma Desktop lên bản mới nhất rồi mở lại.

## 3. Chạy plugin

1. **Plugins → Development → NutriVision Builder — Tuần 2**.
2. Chờ 5–20 giây. Góc dưới hiện thông báo:
   `NutriVision: đã tạo 28 frame trên 4 trang.`
3. Kiểm tra thanh trang bên trái, sẽ có 4 trang:

| Trang | Nội dung |
|---|---|
| `01 · Design System` | Màu, chữ, khoảng cách, bo góc, icon, component, quy ước đặt tên |
| `02 · Screen Flow` | Sơ đồ 14 frame + luồng phụ |
| `03 · Wireframe` | 14 khung xám W01–W14 (chỉ bố cục) |
| `04 · Mockup` | 14 khung màu M01–M14 |

4. Mở trang **04 · Mockup**, chọn các frame và bấm **Present** (▶) để xem dạng điện thoại.
5. Chia sẻ: **Share → Copy link** (quyền *Anyone with the link can view*).

> File tự lưu và đồng bộ. Mở https://www.figma.com/files trên web sẽ thấy đúng file này.

---

## 4. Cấu trúc bản thiết kế

**14 màn hình** (mỗi màn có bản W và M):

| # | Màn hình | # | Màn hình |
|---|---|---|---|
| 01 | Onboarding | 08 | Chọn khẩu phần |
| 02 | Đăng nhập | 09 | Nhật ký ngày |
| 03 | Đăng ký | 10 | Tìm kiếm món |
| 04 | Thiết lập hồ sơ | 11 | Chi tiết mục nhật ký |
| 05 | Dashboard | 12 | Thống kê |
| 06 | Camera | 13 | Cá nhân & cài đặt |
| 07 | Kết quả nhận diện | 14 | Trạng thái rỗng & lỗi |

**Token chính** (chi tiết ở trang Design System):

- Khung: **390 × 844**, lề 16, lưới 4/8 pt, bo góc 12/16/24/99.
- Màu: Primary `#10B981`, Secondary `#F59E0B`, AI `#6366F1`.
- Macro: Đạm `#3B82F6`, Carb `#F59E0B`, Béo `#EF4444`, Xơ `#10B981`, Natri `#8B5CF6`.
- Font: **Inter** (Regular / Medium / Semi Bold / Bold).
- Icon: **Lucide**, nét 2 px, 24 × 24.

**Quy ước đặt layer:** `M05 · Dashboard` → khối `calo-card`, `macro-card`, `meals` → icon `icon/home`, ảnh `photo`. Mỗi layer ánh xạ 1–1 sang một Composable khi dựng Android.

---

## 5. Sửa plugin & build lại

Nguồn nằm trong `src/`:

| File | Nội dung |
|---|---|
| `01_core.js` | Token màu/font, helper `frame/txt/place`, icon, ảnh, vòng calo |
| `02_atoms.js` | Button, input, chip, card, hàng macro, hàng bữa ăn |
| `10_screens_a.js` | Màn 01 → 07 |
| `11_screens_b.js` | Màn 08 → 14 |
| `20_designsystem.js` | Trang Design System |
| `21_flow.js` | Trang Screen Flow |
| `99_main.js` | Lắp ráp, tạo 4 trang, chạy |

Sau khi sửa, chạy:

```powershell
cd E:\LTDD\Tuan2\figma-plugin
powershell -ExecutionPolicy Bypass -File .\build.ps1
node --check code.js
```

Rồi trong Figma: **Plugins → Development → NutriVision Builder — Tuần 2** (chạy lại).

> Mỗi lần chạy sẽ **tạo thêm** 4 trang mới. Muốn chạy lại sạch: xoá 4 trang cũ trước, hoặc tạo file Figma mới rồi chạy.

### Xem trước không cần Figma

```powershell
node tools\figma-mock.js          # xuất preview/*.svg + report.txt
powershell -File tools\sheet.ps1  # contact sheet PNG (cần Edge)
```

`tools/figma-mock.js` là shim API Figma tối giản — chỉ để bắt lỗi runtime và kiểm tra tràn khung, **không thay thế Figma thật**.

---

## 6. Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| Không có `Plugins → Development` | Đang dùng Figma web → mở Figma Desktop |
| `Cannot find font Inter Semi Bold` | Cài Inter từ Google Fonts, khởi động lại Figma; hoặc sửa `FONT` trong `src/01_core.js` cho khớp tên style |
| Plugin chạy nhưng không thấy frame | Chọn đúng trang `03`/`04`; bấm **Zoom to fit** (Shift+1) |
| Chạy lại bị trùng trang | Xoá 4 trang cũ trước khi chạy lại |
| Ảnh món ăn là khối gradient | Đúng thiết kế: ảnh thật thay khi demo, tránh dùng ảnh có bản quyền |
