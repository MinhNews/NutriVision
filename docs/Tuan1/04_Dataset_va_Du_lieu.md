# BỘ DỮ LIỆU & KẾ HOẠCH THU THẬP (làm ngay từ tuần 1)

> Đây là phần **quyết định thành bại** của đề tài. Bắt đầu sớm, mỗi tuần 20 món.

---

## 1. Danh sách 15 nhãn AI sẽ huấn luyện

Chọn món **phổ biến, dễ phân biệt bằng mắt, dễ tìm ảnh**, ưu tiên món có hình dạng đặc trưng:

| # | Nhãn (file) | Tên hiển thị | Vì sao chọn |
|---|---|---|---|
| 1 | `pho_bo` | Phở bò | Món quốc hồn, hình dạng đặc trưng |
| 2 | `pho_ga` | Phở gà | Khác màu nước dùng với phở bò |
| 3 | `bun_bo_hue` | Bún bò Huế | Màu đỏ, dễ phân biệt |
| 4 | `bun_cha` | Bún chả | Có chả viên/vỉ nướng |
| 5 | `bun_rieu` | Bún riêu | Màu cà chua |
| 6 | `com_tam` | Cơm tấm | Sườn nướng + cơm hạt |
| 7 | `com_trang` | Cơm trắng | Lớp nền dễ |
| 8 | `banh_mi` | Bánh mì | Hình dạng rất đặc trưng |
| 9 | `goi_cuon` | Gỏi cuốn | Bánh tráng trong, dễ nhận |
| 10 | `cha_gio` | Chả giò | Vàng, thuôn dài |
| 11 | `banh_xeo` | Bánh xèo | Vàng, tròn, lớn |
| 12 | `mi_quang` | Mì Quảng | Sợi vàng, ít nước |
| 13 | `rau_xao` | Rau xào | Nhóm rau |
| 14 | `trung_luoc` | Trứng luộc | Rất dễ |
| 15 | `trai_cay` | Trái cây (đĩa) | Nhóm tráng miệng |

> Có thể đổi 1–2 nhãn khó (bún riêu, mì Quảng) sang **"Cơm gà"**, **"Đậu hũ"**, **"Salad"** nếu thu ảnh khó.

---

## 2. Hướng dẫn thu ảnh huấn luyện

| Yêu cầu | Chi tiết |
|---|---|
| Số ảnh/lớp | **80–120 ảnh** (tối thiểu 50) |
| Đa dạng | 3–5 góc chụp (trên xuống, 45°), 3 điều kiện sáng (trong nhà, ngoài trời, đèn vàng), 2–3 loại bát/đĩa, có/không có người/bàn tay |
| Nền | Có nền bàn, khăn, menu — **không** chỉ ảnh nền trắng |
| Chất lượng | Rõ nét, **không** dùng ảnh có watermark, ảnh AI-generated, ảnh quá mờ |
| Cân bằng | Mỗi lớp số ảnh chênh nhau ≤ 20% để tránh model thiên vị |
| Chia tập | 80% train / 10% validation / 10% test (Teachable Machine tự chia, nhưng nên giữ 1 bộ test riêng) |
| Nguồn | Ảnh tự chụp (ưu tiên) + ảnh có giấy phép free (Unsplash, Pexels, Pixabay) — **ghi nguồn** trong báo cáo |
| Tổ chức | `dataset/<nhan>/<nhan>_001.jpg` … đặt trên Google Drive chung của nhóm |

**Cách làm nhanh (1 buổi cho cả nhóm):**
- Mỗi người phụ trách 5 nhãn → mỗi nhãn 40 ảnh mạng + 10 ảnh tự chụp = 50 ảnh.
- Train bằng **Teachable Machine** (Image Project → Standard 224×224) → Export TensorFlow Lite (Float32 hoặc quantized).
- Đặt file vào `app/src/main/assets/nutrivision_food.tflite`, kèm `labels.txt`.

**Lưu ý kỹ thuật:**
- Tiền xử lý phải **giống hệt** lúc train: resize 224×224, chuẩn hóa pixel về `[0,1]` hoặc `[-1,1]` tùy model.
- Model lượng tử hóa (`quantized`) nhỏ (~1–3 MB) nhưng giảm độ chính xác nhẹ → dùng Float16 nếu APK cho phép.
- Đo thời gian inference mục tiêu: **< 150 ms/ảnh** trên máy tầm trung.

---

## 3. Nguồn dữ liệu dinh dưỡng

| Nguồn | Dùng cho | Ghi chú |
|---|---|---|
| **Bảng thành phần dinh dưỡng thực phẩm Việt Nam** (Viện Dinh dưỡng QG) | Món Việt | Nguồn chuẩn nhất, ưu tiên số 1 |
| USDA FoodData Central | Nguyên liệu, món quốc tế | Miễn phí, có API |
| Nhãn dinh dưỡng trên bao bì | Đồ uống, sữa, bánh | Chụp lại làm minh chứng |
| CalorieNinjas / Edamam | Bổ sung, đối chiếu | Free tier giới hạn |

**Quy tắc ghi dữ liệu:**
1. Mọi số liệu quy về **trên 100g phần ăn được** (bỏ xương, bỏ hạt).
2. Ghi rõ **trạng thái** (sống/chín) và **nguồn** ở cột `Nguon`.
3. Sai số chấp nhận ±10% giữa các nguồn — nếu lệch nhiều, chọn nguồn Việt Nam.
4. Cột `TrangThai`: `da_kiem_chung` / `so_bo` / `can_khao_sat`.

---

## 4. Phân công thu dữ liệu tuần 1 (20 món)

| Người | Món phụ trách |
|---|---|
| TV1 | Phở bò, phở gà, bún bò Huế, bún chả, bún riêu, cơm tấm, cơm trắng |
| TV2 | Bánh mì, gỏi cuốn, chả giò, bánh xèo, mì Quảng, rau xào, trứng luộc |
| TV3 | Trái cây (chuối/táo/xoài), sữa chua, trà sữa, cà phê sữa, bánh flan, sinh tố bơ, canh chua |

File mẫu: **`05_foods_seed_template.csv`** (đã có 28 dòng, số liệu **tham khảo – phải kiểm chứng lại** trước khi bảo vệ).

---

## 5. Cấu trúc bảng `foods` (dự kiến – chốt ở tuần 3)

```sql
CREATE TABLE foods (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  name          TEXT    NOT NULL,              -- "Phở bò"
  search_name   TEXT    NOT NULL,              -- "pho bo" (bỏ dấu, để tìm kiếm)
  category      TEXT    NOT NULL,              -- Món nước / Cơm / Bánh / Rau / Trái cây / Đồ uống
  kcal_per_100g REAL    NOT NULL,
  protein_g     REAL    NOT NULL DEFAULT 0,
  carb_g        REAL    NOT NULL DEFAULT 0,
  fat_g         REAL    NOT NULL DEFAULT 0,
  fiber_g       REAL    NOT NULL DEFAULT 0,
  sodium_mg     REAL    NOT NULL DEFAULT 0,
  default_portion_g INTEGER NOT NULL,          -- khẩu phần mặc định (1 tô = 500g)
  portion_label TEXT    NOT NULL,              -- "1 tô vừa"
  image_url     TEXT,
  ai_label      TEXT,                          -- map với nhãn model, null nếu không có
  source        TEXT,
  is_custom     INTEGER NOT NULL DEFAULT 0     -- 1 = người dùng tự thêm
);
```

**Công thức tính khẩu phần:**
```
kcal   = kcal_per_100g * gram / 100
macro  = macro_per_100g * gram / 100
```

**Công thức TDEE (Mifflin–St Jeor):**
```
BMR (nam)   = 10*kg + 6.25*cm - 5*tuổi + 5
BMR (nữ)    = 10*kg + 6.25*cm - 5*tuổi - 161
TDEE        = BMR * hệ số vận động
  hệ số: 1.2 (ít vận động) / 1.375 (nhẹ) / 1.55 (vừa) / 1.725 (nhiều)
Mục tiêu giảm cân  = TDEE - 500 kcal
Mục tiêu tăng cân  = TDEE + 300 kcal
```
