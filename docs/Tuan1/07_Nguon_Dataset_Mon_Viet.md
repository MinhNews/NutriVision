# NGUỒN DATASET MÓN VIỆT — Tải ở đâu, dùng cái nào, cần tự chụp bao nhiêu

> **Trả lời ngắn:** Có sẵn dataset món Việt, không cần chụp từ đầu. Nhưng **vẫn phải tự chụp ~20–30 ảnh/lớp** bằng chính điện thoại sẽ dùng để demo — vì ảnh trên mạng khác ảnh thật của bạn (domain shift).

---

## 1. Các dataset có sẵn (đã kiểm tra còn truy cập được)

| # | Dataset | Quy mô | Lớp | Giấy phép | Link |
|---|---|---|---|---|---|
| 1 | **30VNFoods** | 25.136 ảnh (~4.5 GB) | 30 món Việt | Repo MIT, ảnh gốc sưu tầm (ghi nguồn) | `kaggle.com/datasets/quandang/vietnamese-foods` |
| 2 | **VinaFood21** | 13.950 ảnh | 21 món | NC (nghiên cứu) | `arxiv.org/abs/2108.02929` • `github.com/nguyenvd-uit/uit-together-dataset` |
| 3 | **VietFood67** | 33.000 ảnh | 68 món + bounding box | CC BY-NC-SA 4.0 | `kaggle.com/datasets/thomasnguyen6868/vietfood68` |
| 4 | **Vietnamese Street Food** | ~3.500 ảnh (222 MB) | 14 món | **CC BY 4.0** | `huggingface.co/datasets/HoagMin/Vietnamese_StreetFood_Dataset` |
| 5 | **Roboflow Universe** | 1.000–5.000 ảnh/project | nhiều project nhỏ | tuỳ project | `universe.roboflow.com` (search "vietnamese food") |
| 6 | **UIT-TASTET21** | ~77.000 ảnh | 18 món ngày Tết | nghiên cứu | `github.com/nguyenvd-uit` |

**Nhận xét nhanh:**

- **30VNFoods là lựa chọn số 1** cho đồ án: 30 lớp, đúng 12/15 nhãn nhóm cần, có paper IEEE + code tham khảo + top-5 accuracy 96% để so sánh.
- **Vietnamese Street Food (HF)** là lựa chọn số 2: nhỏ, tải nhanh, **license CC BY 4.0 rõ ràng** (an toàn nhất về pháp lý), lại có bounding box (YOLO format).
- **VietFood67** mạnh nhất về số lớp (68) và có bounding box — dùng nếu sau này muốn làm "nhiều món trong 1 ảnh".
- **VinaFood21** trùng lặp nhiều với 30VNFoods, chỉ nên lấy để **bổ sung lớp thiếu**.
- **Roboflow** tiện cho việc export đúng format + có model pretrained, nhưng mỗi project chỉ vài trăm ảnh.

---

## 2. Mỗi dataset có món gì? (đối chiếu 15 nhãn của nhóm)

| Nhãn nhóm cần | 30VNFoods | Street Food (HF) | VinaFood21 | Cách lấy |
|---|:--:|:--:|:--:|---|
| `pho_bo` – Phở bò | ✅ | ✅ | ✅ | Dataset |
| `pho_ga` – Phở gà | ✅ | – | ✅ | Dataset |
| `bun_bo_hue` – Bún bò Huế | ✅ | ✅ | ✅ | Dataset |
| `bun_cha` – Bún chả | ✅ | ✅ | ✅ | Dataset |
| `bun_rieu` – Bún riêu | ✅ | – | ✅ | Dataset |
| `com_tam` – Cơm tấm | ✅ | ✅ | ✅ | Dataset |
| `com_trang` – Cơm trắng | ✅ | – | – | Dataset + tự chụp |
| `banh_mi` – Bánh mì | ✅ | ✅ | ✅ | Dataset |
| `goi_cuon` – Gỏi cuốn | ✅ | ⚠️ | ✅ | Dataset (**kiểm tra nhãn**) |
| `cha_gio` – Chả giò | ✅ | ⚠️ | ✅ | Dataset (**kiểm tra nhãn**) |
| `banh_xeo` – Bánh xèo | ✅ | – | ✅ | Dataset |
| `mi_quang` – Mì Quảng | ✅ | – | ✅ | Dataset |
| `rau_xao` – Rau xào | ❌ | ❌ | ❌ | **Tự chụp** (dễ) |
| `trung_luoc` – Trứng luộc | ❌ | ❌ | ❌ | **Tự chụp** (rất dễ) |
| `trai_cay` – Đĩa trái cây | ❌ | ❌ | ❌ | **Tự chụp** (rất dễ) |

Kết luận: **12/15 lớp lấy từ dataset**, 3 lớp còn lại tự chụp trong 1 buổi (rau xào, trứng luộc, trái cây — đều là vật dễ mua, dễ chụp).

> ⚠️ **Cảnh báo nhãn:** trong dataset HF, lớp số 12 ghi "Spring Rolls – Gỏi cuốn" nhưng ảnh có thể là **chả giò chiên**. Tương tự "Bún chả" (số 4) và "Bún thịt nướng" (số 6) rất dễ lẫn. **Phải mở xem 20 ảnh mỗi lớp trước khi dùng** — 15 phút kiểm tra cứu được cả buổi demo.

---

## 3. Combo khuyến nghị cho nhóm

```
Nền số lượng:   30VNFoods          → 100 ảnh/lớp × 12 lớp = 1.200 ảnh
Bổ sung:        Street Food (HF)   → 20–30 ảnh/lớp cho các lớp trùng
Lớp thiếu:      Tự chụp            → 60–80 ảnh × 3 lớp = ~200 ảnh
Miền dữ liệu:   Tự chụp            → 20–30 ảnh/lớp × 15 lớp = ~350 ảnh
---------------------------------------------------------------
Tổng: ~1.700–1.800 ảnh cho 15 lớp  (mục tiêu 100–130 ảnh/lớp)
```

### Vì sao **bắt buộc** phải có ảnh tự chụp?

Ảnh trên mạng được chụp bằng máy xịn, ánh sáng đẹp, có phông nền. Ảnh demo của bạn là **điện thoại tầm trung, đèn huỳnh quang lớp học, bát inox**. Model học trên ảnh mạng thường **rớt 15–25% accuracy** khi gặp ảnh thật.

Cách xử lý chuẩn (rất dễ, chỉ thêm 1 bước):
1. Train trên dataset + ảnh tự chụp trộn lẫn.
2. Dành **10% ảnh tự chụp làm test set** → con số accuracy báo cáo sẽ **trung thực** và hội đồng tin.
3. Nếu accuracy test thật thấp hơn nhiều so với test trên ảnh dataset → đó chính là "hạn chế" để viết vào báo cáo (rất được điểm vì biết đánh giá đúng).

---

## 4. Cách tải

### 4.1 Kaggle (30VNFoods, VietFood67)

```bash
pip install kaggle
# Lấy token: kaggle.com → Account → Create New API Token → đặt kaggle.json vào ~/.kaggle/
kaggle datasets download -d quandang/vietnamese-foods -p ./data/30vn --unzip
kaggle datasets download -d thomasnguyen6868/vietfood68 -p ./data/vietfood67 --unzip
```

> 30VNFoods nặng ~4.5 GB. Nếu mạng yếu: tải trực tiếp trên web, chọn từng thư mục lớp cần (12 lớp) thay vì cả bộ.

### 4.2 Hugging Face (Street Food — khuyến nghị vì nhẹ + license rõ)

```bash
pip install -U "huggingface_hub[cli]"
huggingface-cli download HoagMin/Vietnamese_StreetFood_Dataset \
  --repo-type dataset --local-dir ./data/streetfood
```

### 4.3 Roboflow

Vào `universe.roboflow.com` → search "vietnamese food" → chọn project → **Download Dataset** → format **Folder Structure** (cho classification) hoặc **YOLOv8** (cho detection). Cần tài khoản free.

---

## 5. Quy trình làm sạch (bắt buộc, đừng bỏ)

```
Tải về → Lọc lớp cần → Xem mắt 20 ảnh/lớp → Khử trùng lặp
      → Cân bằng số ảnh → Đổi tên theo chuẩn → Chia train/test → Train
```

### 5.1 Khử trùng lặp (dataset web-scrape có rất nhiều ảnh trùng)

Ảnh trùng giữa train và test sẽ làm **accuracy ảo tăng vọt** → hội đồng hỏi là lộ.

```python
# pip install pillow imagehash
from pathlib import Path
from PIL import Image
import imagehash, shutil

SRC = Path("./data/30vn/Images")
DST = Path("./data/clean")
seen = set()

for cls in sorted(p for p in SRC.iterdir() if p.is_dir()):
    out = DST / cls.name
    out.mkdir(parents=True, exist_ok=True)
    for img_path in cls.glob("*.*"):
        try:
            h = str(imagehash.dhash(Image.open(img_path).convert("RGB"), hash_size=16))
        except Exception:
            continue
        if h in seen:            # ảnh trùng -> bỏ
            continue
        seen.add(h)
        shutil.copy(img_path, out / img_path.name)

print("Xong. Số ảnh sau khử trùng:", len(seen))
```

### 5.2 Cân bằng lớp

Mỗi lớp chênh nhau **≤ 20%**. Nếu lớp `pho_bo` có 1.200 ảnh mà `rau_xao` chỉ 60 ảnh → lấy ngẫu nhiên 100 ảnh `pho_bo`, hoặc dùng `class_weight` khi train ngoài.

### 5.3 Đặt tên chuẩn

```
dataset/<nhan>/<nhan>_0001.jpg
ví dụ: dataset/pho_bo/pho_bo_0001.jpg
```
Không dấu, không khoảng trắng — tránh lỗi khi đọc `labels.txt` trên Android.

### 5.4 Kiểm tra chất lượng (checklist 10 phút/lớp)

- [ ] Không có ảnh watermark logo báo/tạp chí
- [ ] Không có ảnh nhiều món (nếu train classification 1 nhãn)
- [ ] Không có ảnh món khác bị dán nhãn sai
- [ ] Không có ảnh quá mờ / quá tối
- [ ] Không có ảnh động vật, người chiếm chủ thể
- [ ] Đủ đa dạng góc chụp (không phải 100 ảnh cùng 1 góc)

---

## 6. Vấn đề pháp lý (nói trước trong báo cáo là an toàn nhất)

| Dataset | Tình trạng | Cách ghi trong báo cáo |
|---|---|---|
| 30VNFoods | Ảnh sưu tầm từ web (Foody...), repo code MIT | "Sử dụng cho mục đích học tập/nghiên cứu, có trích dẫn tác giả và paper gốc; không phân phối lại dataset" |
| VietFood67 | **CC BY-NC-SA 4.0** | Được dùng phi thương mại, **phải ghi công** + chia sẻ lại theo cùng giấy phép |
| Street Food (HF) | **CC BY 4.0** | An toàn nhất: chỉ cần ghi công |
| VinaFood21 | Nghiên cứu | Trích dẫn paper arXiv:2108.02929 |

**Mẫu câu đưa vào báo cáo:**
> "Bộ dữ liệu huấn luyện được tổng hợp từ 30VNFoods [1] (25.136 ảnh, 30 lớp), Vietnamese Street Food Dataset [2] (CC BY 4.0) và 350 ảnh do nhóm tự thu thập. Dữ liệu chỉ dùng cho mục đích học tập, không phân phối lại."

---

## 7. Việc cần làm ngay (tuần 1 → tuần 2)

| Việc | Ai | Thời gian |
|---|---|---|
| Tạo tài khoản Kaggle + HF, tải 2 dataset | TV2 | 1 buổi (chờ tải) |
| Mở xem 20 ảnh/lớp, xác nhận nhãn đúng | Cả 3 (chia lớp) | 1 giờ |
| Chạy script khử trùng lặp + cân bằng | TV2 | 30 phút |
| Tự chụp 3 lớp thiếu + 20 ảnh/lớp còn lại | Cả 3 | 1 buổi tối |
| Train bản thử trên Teachable Machine | TV1 | 30 phút |
| Ghi lại accuracy + thời gian inference | TV1 | 15 phút |

---

## 8. Nếu muốn model "xịn" hơn Teachable Machine

Dùng **Google Colab** + MobileNetV2 (transfer learning), có sẵn ảnh dataset:

- Kiểm soát được **data augmentation**, **class weight**, **early stopping**
- Xuất được **confusion matrix** đẹp để chụp vào slide
- Export TFLite bằng `tf.lite.TFLiteConverter` (Float16)
- Thời gian: ~30–45 phút train cho 15 lớp × 1.800 ảnh trên Colab free

→ Nếu cần, nhóm có thể nhờ viết sẵn notebook `.ipynb` để chỉ việc bấm Run All.

**Tài liệu tham khảo để trích dẫn:**
1. Dang et al., *A Dataset for Vietnamese Food Images Recognition*, IEEE, 2021.
2. Nguyen et al., *VinaFood21: A Novel Dataset for Evaluating Vietnamese Food Recognition*, arXiv:2108.02929, 2021.
3. HoagMin, *Vietnamese Street Food Dataset*, Hugging Face, CC BY 4.0.
4. Nguyen Viet Hoang Nam et al., *VietFood67*, Kaggle, CC BY-NC-SA 4.0.
