# ĐẶC TẢ WIREFRAME & MOCKUP FIGMA — NUTRIVISION (TUẦN 2)

> **Dành cho TV3 (UI/UX & QA Lead)** và cả nhóm khi dựng Compose ở Tuần 4.
> Bản thiết kế được **sinh bằng plugin Figma** (`figma-plugin/`), không vẽ tay. Tài liệu này là bản đặc tả để đối chiếu, nghiệm thu và ánh xạ sang code.

---

## 1. Phạm vi bàn giao

| Trang Figma | Nội dung | Số lượng |
|---|---|---|
| `01 · Design System` | Token màu, kiểu chữ, khoảng cách, bo góc, icon, component, quy ước đặt tên | 1 bảng lớn |
| `02 · Screen Flow` | Sơ đồ 14 frame, 4 nhóm màu, luồng phụ | 1 sơ đồ |
| `03 · Wireframe` | 14 khung xám W01–W14, chỉ bố cục và thứ bậc | 14 frame |
| `04 · Mockup` | 14 khung màu M01–M14, dùng đúng token | 14 frame |

**Kích thước chuẩn:** `390 × 844 px` (tỉ lệ iPhone 14 / Pixel 7). Toàn bộ toạ độ trong tài liệu tính theo khung này.
**Lề màn hình:** 16 px. **Lưới:** 4/8 pt. **Bo góc:** 12 (input, ảnh nhỏ) · 16 (card) · 24 (bottom sheet) · 99 (pill).

---

## 2. Quy chuẩn dùng chung

### 2.1. Màu

| Nhóm | Token | Mã | Dùng cho |
|---|---|---|---|
| Thương hiệu | Primary | `#10B981` | Nút chính, tab active, vòng calo |
| | Primary Deep | `#059669` | Chữ trên nền Primary Soft |
| | Primary Soft | `#D1FAE5` | Chip chọn, nền card nổi bật |
| | Primary Mist | `#ECFDF5` | Khối gợi ý, nền thông tin |
| | Secondary | `#F59E0B` | Calo vượt mục tiêu, bữa sáng, streak |
| | AI Accent | `#6366F1` | Nhãn “AI trên thiết bị”, gợi ý model |
| Dinh dưỡng | Protein | `#3B82F6` | Đạm |
| | Carbs | `#F59E0B` | Tinh bột |
| | Fat | `#EF4444` | Chất béo |
| | Fiber | `#10B981` | Chất xơ |
| | Sodium | `#8B5CF6` | Natri |
| Trung tính | Background | `#F8FAFC` | Nền ứng dụng |
| | Surface | `#FFFFFF` | Nền card |
| | Border | `#E2E8F0` | Viền, divider |
| | Text | `#0F172A` | Tiêu đề, số liệu |
| | Text Secondary | `#64748B` | Mô tả, đơn vị |
| | Danger | `#EF4444` | Lỗi, vượt ngưỡng |

Màu dinh dưỡng **cố định toàn app**: người dùng nhìn màu là nhận ra chỉ số, không đổi giữa các màn.

### 2.2. Kiểu chữ — Inter

| Style | Cỡ / line-height | Dùng cho |
|---|---|---|
| Display | 32 / 40 · Bold | Số calo lớn |
| Headline | 24 / 32 · Bold | Tên món, tiêu đề lớn |
| Title | 18 / 26 · SemiBold | Tiêu đề khối |
| Title | 16 / 24 · SemiBold | Tiêu đề màn phụ |
| Body | 14 / 22 · Regular | Mô tả |
| Label | 13 / 18 · SemiBold | Nhãn nút, tên món trong danh sách |
| Caption | 11 / 16 · Medium | Đơn vị, nguồn, ghi chú |

### 2.3. Icon & ảnh

- **Lucide**, nét 2 px, khung 24 × 24. Không trộn icon đặc với icon nét.
- Icon trong nền tròn: `icon-circle` 30–48 px, nền Primary Mist / Accent Soft.
- **Ảnh món ăn:** placeholder gradient + icon `bowl`. Khi demo thay bằng ảnh chụp thật của nhóm; không dùng ảnh có bản quyền.

### 2.4. Quy ước đặt tên layer

```text
M05 · Dashboard          → frame màn hình
  calo-card              → khối vòng calo
    ring/69%
    stat-row
  macro-card             → 3 thanh macro
  meals                  → danh sách bữa
    meal-row/Bữa sáng
icon/home                → icon
photo                    → ảnh placeholder
btn/Lưu vào bữa trưa     → button
```

Mỗi layer ánh xạ 1–1 sang một Composable: `calo-card` → `CalorieCard()`, `meal-row` → `MealRow()`, `macro-card` → `MacroCard()`.

---

## 3. Danh sách 14 màn hình

| # | Frame | Nhóm | Mục đích | Điểm vào |
|---|---|---|---|---|
| 01 | Onboarding | Tài khoản | Giới thiệu 3 giá trị: nhận diện trên máy, khẩu phần Việt, dữ liệu cục bộ | Lần đầu mở app |
| 02 | Đăng nhập | Tài khoản | Vào lại tài khoản cục bộ | Từ 01, 13 |
| 03 | Đăng ký | Tài khoản | Tạo tài khoản, kiểm tra mật khẩu | Từ 02 |
| 04 | Thiết lập hồ sơ | Tài khoản | Nhập giới tính, chiều cao, cân nặng, mức vận động → TDEE | Sau 03 |
| 05 | Dashboard | Lõi | Vòng calo, macro, 4 bữa, lối tắt camera | Sau 04, tab Trang chủ |
| 06 | Camera | Lõi | Chụp ảnh hoặc chọn từ thư viện | Nút camera giữa nav |
| 07 | Kết quả nhận diện | Lõi | Top-3 món + độ tin cậy, đổi món, tìm thủ công | Sau 06 |
| 08 | Chọn khẩu phần | Lõi | Chọn tô/bát/cái hoặc số gram → dinh dưỡng | Sau 07, từ 10 |
| 09 | Nhật ký ngày | Lõi | Danh sách mục theo bữa, sửa/xóa, thêm món | Tab Nhật ký |
| 10 | Tìm kiếm món | Tiện ích | Lọc theo nhóm món, chọn món thủ công | Từ 05, 07, 09 |
| 11 | Chi tiết mục nhật ký | Tiện ích | Xem lại dinh dưỡng, sửa khẩu phần, xóa | Chạm một mục ở 09 |
| 12 | Thống kê | Tiện ích | Calo 7/30 ngày, tỉ lệ macro, gợi ý | Tab Thống kê |
| 13 | Cá nhân & cài đặt | Tiện ích | Hồ sơ, mục tiêu, nhắc nhở, dữ liệu | Tab Cá nhân |
| 14 | Trạng thái rỗng & lỗi | Trạng thái | Bộ trạng thái dùng lại ở 05, 07, 09, 10 | Không phải màn riêng |

---

## 4. Đặc tả từng màn

### 01 · Onboarding

- **Ảnh hero** cao 372, placeholder gradient + icon tô.
- **Dots** 3 chấm (chấm 1 dài 20 px), căn giữa, y = 396.
- **Tiêu đề:** “Ăn uống thông minh,\nngay từ bữa đầu tiên” — 28/36 Bold.
- **Mô tả:** “Chụp ảnh món ăn, NutriVision nhận diện ngay trên máy và cho bạn biết calo, đạm, carb, béo — không cần mạng.”
- **3 dòng giá trị** (icon tròn 30): nhận diện 15 món Việt bằng model trên thiết bị · khẩu phần theo tô/bát/cái · dữ liệu nằm trên máy.
- **CTA:** “Bắt đầu ngay” (342 × 52).
- **Link phụ:** “Đã có tài khoản? Đăng nhập”.

### 02 · Đăng nhập

- Nút back 24, tiêu đề “Chào mừng trở lại” 26 Bold, mô tả 1 dòng.
- Input Email (icon `mail`), input Mật khẩu (icon `lock`, trailing `eye`), link “Quên mật khẩu?” canh phải.
- CTA “Đăng nhập” 342 × 52; divider “hoặc”; nút ghost “Tạo tài khoản mới”.
- Khối ghi chú: “Tài khoản lưu cục bộ trên máy. Không cần internet.”
- **Không có** đăng nhập Google/Firebase.

### 03 · Đăng ký

- Tiêu đề “Tạo tài khoản”; mô tả “Mất 30 giây. Nhật ký bữa ăn của bạn được lưu riêng trên thiết bị.”
- Email; Mật khẩu (đang focus, viền Primary 1.5); gợi ý “Tối thiểu 6 ký tự.”; Nhập lại mật khẩu.
- Checkbox đồng ý điều khoản (đã tick) + 2 dòng chữ 11.5.
- CTA “Tạo tài khoản”; link “Đã có tài khoản? Đăng nhập”.

### 04 · Thiết lập hồ sơ

- Thanh bước “Bước 2 / 3” + “Chỉ số cơ thể”, progress 66%.
- Tiêu đề “Cho chúng tôi biết về bạn”; mô tả “Dùng để tính TDEE và mục tiêu calo mỗi ngày.”
- 2 thẻ giới tính 165 × 82, thẻ chọn có viền Primary + nền Primary Mist.
- 2 input 165: Chiều cao 170 cm (icon `scale`) · Cân nặng 65 kg.
- Chip mức vận động: Ít vận động · **Vừa phải** · Nhiều.
- Card “MỤC TIÊU ĐỀ XUẤT”: **1.940 kcal/ngày**, phụ chú “TDEE 2.430 kcal · giảm 20% để giảm cân an toàn (0,3–0,5 kg/tuần).”
- CTA “Hoàn tất & vào trang chủ”; ghi chú “Có thể đổi lại bất cứ lúc nào trong Cài đặt”.

### 05 · Dashboard

- **Header 96:** “Chào buổi sáng, Minh 👋” 12 + “Hôm nay, 05/11” 19 Bold; avatar 38 bên phải.
- **Card calo 358 × 196:** vòng 132 (69%), giữa là **1.250** + “kcal còn lại”; hàng dưới: chấm xanh “Đã nạp 550 kcal” và “Mục tiêu 1.800 kcal”.
- **Card macro:** 3 hàng Đạm `42 / 110 g` (38%), Carb `98 / 220 g` (45%), Béo `24 / 60 g` (40%); thanh 7 px, màu theo bảng dinh dưỡng.
- **“Bữa ăn hôm nay”** + link “Xem nhật ký”.
- **4 hàng bữa** 358 × 58: Sáng 340 kcal · Trưa 620 kcal · Tối “Chưa ghi món nào” + nút `+` · Phụ “Chưa ghi món nào” + nút `+`.
- **Bottom nav 84** + FAB camera 56 nhô lên giữa, nhãn “Chụp món”.

### 06 · Camera

- Nền tối `#0B1220` + placeholder ảnh camera.
- Khung ngắm 286 × 286 bo 24, viền trắng 2 px, căn giữa (y = 268).
- Gợi ý: “Đặt món ăn vào giữa khung hình” + “Giữ máy chắc, đủ sáng để AI nhận diện chính xác”.
- Top: back trắng, nút flash tròn 36 (nền trắng 14%).
- Đáy: thư viện ảnh 48 · **shutter 76** (lõi Primary 62) · tìm món (`search` 22).
- Nhãn dưới: “Thư viện” / “Tìm món”.

### 07 · Kết quả nhận diện

- Ảnh 390 × 320; back tròn 36; pill “AI trên thiết bị · TFLite” (Accent Soft).
- **Sheet** bắt đầu y = 296, bo 24, padding top 34:
  - Tên món **Phở bò tái nạm** 24 Bold + pill **89%**; phụ “1 tô vừa · 500 g”.
  - Nhãn “Độ tin cậy của model” + thanh 350 × 8 (89%).
  - “Không đúng món?” + 2 hàng gợi ý: Phở gà 7% · Bún bò Huế 3%.
  - Nút “Tìm món khác trong 100+ món Việt”.
  - Ghi chú: “Model chỉ gợi ý tên món. Dinh dưỡng lấy từ cơ sở dữ liệu nội bộ, không lấy từ AI.”
- CTA cố định: “Tiếp tục chọn khẩu phần” (y = 730).
- **Ngưỡng tin cậy:** dưới 60% → hiện top-3 nổi bật + nút tìm thủ công (xem màn 14).

### 08 · Chọn khẩu phần

- App bar “Chọn khẩu phần” + back.
- Card món 358: ảnh 64 · tên 16 SemiBold · “105 kcal / 100 g · nguồn: Viện Dinh dưỡng VN”.
- “Khẩu phần thực tế” + segmented: Nửa tô · **1 tô** · Tô lớn.
- Hàng nhập: ô `500` gram (174) + gợi ý “≈ 1 tô vừa” (174, nền Primary Mist).
- Card “Dinh dưỡng theo khẩu phần” + pill `500 g`:
  - Lưới 2 × 2: Calo **525** kcal · Đạm **32** g · Carb **55** g · Béo **16** g.
  - Divider; hàng Chất xơ `3,4 g` · Natri `1.980 mg`.
- Cảnh báo đỏ nhạt: “Natri gần chạm ngưỡng 2.000 mg/ngày của WHO.”
- CTA “Lưu vào bữa trưa · 525 kcal”; ghi chú “Có thể sửa lại sau khi lưu”.

> Công thức: `giá trị = định lượng / 100 g × số gram`. Làm tròn calo đến 5, macro đến 1 g, natri đến 10 mg. Nguồn dữ liệu: bảng thành phần dinh dưỡng Việt Nam (Viện Dinh dưỡng), lưu trong Room ở Tuần 3.

### 09 · Nhật ký ngày

- App bar “Nhật ký” + icon lịch.
- Dải 7 ngày 44 × 58; ngày chọn nền Primary, chữ trắng (T4 05).
- Card tổng: **1.550** / “trong 1.800 kcal mục tiêu” + pill “còn 250 kcal”; thanh 86%.
- Danh sách bữa:
  - Sáng: Bánh mì thịt · 1 cái — 340 kcal.
  - Trưa: Phở bò tái · 1 tô vừa — 525 kcal; Trà đá · 1 ly — 0 kcal.
  - Tối: hàng “+ Thêm món”.
  - Phụ: hàng “+ Thêm món”.
- FAB `+` 56 ở góc phải (y = 686), bottom nav tab Nhật ký active.
- Chạm một mục → màn 11.

### 10 · Tìm kiếm món

- App bar “Tìm món” + back + `more`.
- Ô tìm 358 × 48: icon search, từ khóa “phở bò”, nút xóa.
- 4 chip lọc: Tất cả · Món nước · Cơm · **Bún – phở** (active).
- “8 kết quả cho ‘phở bò’”.
- 6 hàng kết quả 358 × 62: ảnh 46 · tên · mô tả (nhóm · khẩu phần) · kcal canh phải.
- Không có kết quả → trạng thái ở màn 14.

### 11 · Chi tiết mục nhật ký

- Ảnh 390 × 220; back tròn 36; nút sửa tròn 36 góc phải.
- Sheet y = 196, bo 24, padding top 34:
  - **Phở bò tái nạm** 22 Bold + pill “1 tô vừa”; phụ “Bữa trưa · 05/11 · 12:20”.
  - Card calo nền Primary Mist: **525** kcal + “22% nhu cầu ngày”.
  - Bảng 5 hàng: Đạm 32 g · Carb 55 g · Béo 16 g · Chất xơ 3,4 g · Natri 1.980 mg.
- Nút ghost “Sửa khẩu phần” (icon edit) → quay lại 08 với dữ liệu cũ.
- Nút danger “Xóa khỏi nhật ký” (icon trash) → xác nhận + snackbar hoàn tác.

### 12 · Thống kê

- App bar “Thống kê” + `more`; segmented 7 ngày / 30 ngày.
- Card trung bình: **1.640** kcal/ngày + pill “−160 kcal”; phụ “6/7 ngày ghi chép đầy đủ · chuỗi 12 ngày”.
- Card biểu đồ cột 330 × 132: 7 cột (30 px), cột vượt mục tiêu màu Secondary; đường nét đứt “mục tiêu 1.800”.
- Card donut: vòng 104 + chú giải Đạm 30% · Carb 45% · Béo 25%.
- Khối gợi ý: “Đạm đang thấp hơn mục tiêu 8 g/ngày. Bữa tối có thể thêm 1 quả trứng hoặc 100 g ức gà.”
- Bottom nav tab Thống kê active.

### 13 · Cá nhân & cài đặt

- Card hồ sơ: avatar 46 · tên · email · chevron.
- Card mục tiêu (Primary Mist): “Mục tiêu 1.800 kcal” + “TDEE 2.250 · giảm cân 20% · 65 kg” + pill “Đổi mục tiêu”.
- Nhóm **TÀI KHOẢN & MỤC TIÊU**: Hồ sơ & chỉ số cơ thể (170 cm · 65 kg) · Mục tiêu calo & macro (1.800 kcal).
- Nhóm **NHẮC NHỞ & GIAO DIỆN**: Nhắc ghi bữa ăn (switch on) · Giờ nhắc 07:30 · 12:00 · 19:00 · Chế độ tối (switch off).
- Nhóm **DỮ LIỆU**: Xuất nhật ký (CSV) · Xóa toàn bộ dữ liệu (đỏ).
- Nút ghost “Đăng xuất”; chân trang “NutriVision 1.0 · dữ liệu lưu trên thiết bị”.
- Bottom nav tab Cá nhân active.

### 14 · Trạng thái rỗng & lỗi

Bộ trạng thái dùng lại, không phải màn riêng:

| Trạng thái | Copy | Hành động |
|---|---|---|
| AI không chắc chắn | “Độ tin cậy dưới 60%: hiện top-3 và nút tìm món thủ công thay vì đoán bừa.” | Tìm món thủ công |
| Nhật ký trống | “Ngày mới chưa có món nào. Gợi ý chụp bữa sáng để bắt đầu chuỗi ngày.” | Chụp bữa sáng |
| Không tìm thấy món | “Không có kết quả cho ‘bánh xèo miền Tây’. Thử từ khoá ngắn hơn.” | Xóa bộ lọc |
| Lưu thành công | Snackbar “Đã thêm vào bữa trưa” + Hoàn tác (5 giây) | Hoàn tác |
| Lỗi dữ liệu | “Không đọc được model. Kiểm tra lại tệp dinh dưỡng và thử lại.” | — |

---

## 5. Wireframe khác Mockup ở đâu

| | Wireframe (W01–W14) | Mockup (M01–M14) |
|---|---|---|
| Màu | Thang xám, không màu thương hiệu | Đúng token |
| Ảnh | Khối xám + icon | Gradient + icon |
| Mục đích | Kiểm tra bố cục, thứ bậc, khoảng cách | Kiểm tra màu, tương phản, nhận diện |
| Khi nào dùng | Trình bày lý do bố cục, sửa nhanh | Chốt giao diện, bàn giao Compose |

Khi review: xem **Wireframe trước** để chốt vị trí, rồi mới xem Mockup. Không sửa màu khi bố cục chưa ổn.

---

## 6. Điều hướng & luồng phụ

- **Luồng chính:** 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11 → 12 → 13 (sơ đồ ở trang `02 · Screen Flow`).
- **Luồng phụ:**
  - 05 → 09 → 11: xem, sửa, xóa mục nhật ký.
  - 05 → 10: tìm món thủ công.
  - 06 → 07: ảnh chụp hoặc ảnh từ thư viện.
- **Bottom nav** xuất hiện ở 05, 09, 12, 13; FAB camera ở giữa luôn mở 06.
- Back luôn quay lại màn trước; không có vòng lặp đóng app đột ngột.

---

## 7. Nghiệm thu

- [ ] Đủ 14 frame ở cả Wireframe và Mockup, đúng tên `W01…W14` / `M01…M14`.
- [ ] Không frame nào có nội dung tràn quá 844 px hoặc ra ngoài lề 16.
- [ ] Mọi màu lấy từ bảng token; không có mã màu tự phát.
- [ ] Mọi text dùng Inter; không lẫn font khác.
- [ ] Trang Design System và Screen Flow mở được, không lỗi font.
- [ ] Chạy plugin trên Figma Desktop thành công, file đồng bộ lên web.
- [ ] Ảnh placeholder được thay bằng ảnh thật trước khi demo (không bắt buộc khi nộp tuần 2).

---

## 8. Cách tạo lại bản thiết kế

Xem `figma-plugin/README.md` — hướng dẫn từng bước: cài Figma Desktop → Import plugin from manifest → Run. Nguồn plugin nằm trong `figma-plugin/src/`, build bằng `figma-plugin/build.ps1`.
