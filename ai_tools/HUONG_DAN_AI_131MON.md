# HƯỚNG DẪN LÀM PHẦN AI — NUTRIVISION 131 MÓN

> **Phạm vi:** chỉ phần AI (dataset → train → model.tflite). **Chưa đụng code Android.**
> **Kiến trúc (chốt):** hybrid 3 lớp —
> 1. **On-device:** MobileNetV3 + TFLite nhận diện 131 món, không cần mạng.
> 2. **Gemini fallback:** khi model không chắc hoặc người dùng bấm "không phải món này", app gọi Gemini để nhận diện món lạ (cần internet).
> 3. **Lưu trữ:** Room/SQLite trên máy là nguồn chính; Firebase đồng bộ lên cloud khi có mạng.
>
> Phần AI trong tài liệu này chỉ lo lớp 1 và hợp đồng dữ liệu cho lớp 2. Gemini, Room, Firebase là việc Tuần 3.

---

## 0. Chốt lại: làm gì và vì sao

| Hạng mục | Quyết định |
|---|---|
| Số lớp | **131** = 101 món Việt + 30 món ngoại phổ biến |
| Món Việt | Lấy từ dataset `jeandeanny/vn-food-combined-2026` — 103 lớp, 40.432 ảnh, split train/val/test đã lọc trùng |
| Món ngoại | Lấy từ `ethz/food101` — chỉ chọn 30 lớp có trong Food-101 |
| Model | MobileNetV3-Large (mặc định) hoặc Small (nhanh hơn) |
| Đầu ra | `model.tflite` (~5 MB), `labels.txt`, `model_meta.json`, biểu đồ + báo cáo |
| Ngưỡng tin cậy | **0.70** (chốt từ bảng ngưỡng thật, xem 5.6). Trên ngưỡng → top-1; dưới ngưỡng → top-3 + nút gọi Gemini + tìm thủ công |

### Vì sao không dùng 100 nhãn cũ?

- 34/100 nhãn cũ **không có** trong dataset Việt (30 món ngoại + `pho_bo`, `pho_ga`, `xoi_ga`, `cha_gio`).
- Dataset Việt chỉ có **một lớp `pho`** chung, không tách phở bò / phở gà.
- Vì vậy danh sách mới dùng `pho` (hiển thị "Phở"), bỏ 4 nhãn không có dữ liệu, và **thay 10 món ngoại không có trong Food-101** bằng 10 món ngoại phổ biến khác.

> Nếu sau này nhóm tự chụp thêm ảnh phở bò / phở gà, có thể tách lại thành 2 lớp ở vòng huấn luyện sau.

---

## 1. Thư mục `ai_tools` — chỉ có 3 mục cần quan tâm

| Mục | Dùng để |
|---|---|
| `Train_NutriVision_131.ipynb` | **Upload lên Colab.** Đã nhúng sẵn script và 2 file mapping, không cần file nào khác. |
| `HUONG_DAN_AI_131MON.md` | File này — hướng dẫn chạy. |
| `class_summary.csv` | Mở bằng Excel để xem 131 món (id, tên, nhóm, nguồn). |

Hai thư mục còn lại **không cần mở**:

| Thư mục | Nội dung |
|---|---|
| `_source/` | Code và dữ liệu gốc: `train_nutrivision.py`, `class_map.json`, `labels_131.txt`, `class_names_vi.json`, `foods_nutrition.json`, `nutrition_build.py`, `build_classes.py`, `build_notebook.py`, `sync_app_assets.py`. Chỉ dùng khi muốn sinh lại notebook / dựng lại dinh dưỡng / đồng bộ assets / soát lỗi. |
| `_archive/` | Đồ cũ **không dùng nữa**: dataset 125 ảnh lỗi (20 thư mục, 5 rỗng), `labels.txt` 100 nhãn cũ, notebook MobileNetV3 cũ, hướng dẫn Gemini cũ, `download_dataset.py`. |

---

## 2. Chuẩn bị (5 phút)

1. Có tài khoản Google.
2. Vào https://colab.research.google.com → **New notebook** (hoặc upload file `.ipynb` ở trên).
3. Menu **Runtime → Change runtime type → T4 GPU → Save**.
4. Menu **File → Upload notebook** → chọn `Train_NutriVision_131.ipynb`.

> Nên mount Google Drive ở cell số 2 để model không mất khi Colab ngắt kết nối.

---

## 3. Chạy — đúng thứ tự

### Cell 1 — Kiểm tra GPU

Cell này không cài lại TensorFlow (Colab đã có sẵn, cài lại dễ lệch phiên bản CUDA). Nó chỉ cài `datasets`, `pillow`, `tqdm`, `scikit-learn`, `matplotlib` rồi in ra:

```text
tensorflow: 2.xx.x
datasets  : x.x.x
GPU       : [PhysicalDevice(name='/physical_device:GPU:0', device_type='GPU')]
```

Nếu dòng GPU báo `KHONG CO GPU`: quay lại Runtime → Change runtime type → T4 GPU.

### Cell 2 — Mount Google Drive

Colab hiện hộp thoại xin quyền → **Connect to Google Drive** → chọn tài khoản → Allow.

Kết quả sẽ là:
```text
Ket qua se luu tai: /content/drive/MyDrive/NutriVision_AI
```

Cell này **bắt buộc chạy** nếu muốn cell 12 copy kết quả sang Drive. Train vẫn ghi ra `/content/out` cho nhanh.

### Cell 3–5 — Ghi script + 2 file dữ liệu

Ba cell này dùng `%%writefile`, không in ra gì nhiều. Chạy xong kiểm tra thanh file bên trái phải có:
```text
train_nutrivision.py
class_map.json
labels_131.txt
```

### Cell 6 — Tải dataset (khoảng 25–40 phút)

```bash
python train_nutrivision.py prepare --data-dir /content/data --max-train 250 --check-duplicates
```

Quá trình:
1. Tải 5,7 GB dataset Việt + 5,1 GB Food-101.
2. Giải ảnh ra `/content/data/{train,val,test}/<id>/`.
3. Kiểm tra đủ 131 lớp ở cả 3 split.
4. Quét file trùng byte giữa các lớp.

Kỳ vọng cuối log:
```text
[NutriVision] Dataset hop le: du 131 lop o ca 3 split.
[NutriVision] Khong co file trung byte.
```

> Nếu thấy `CANH BAO: ... nhom file trung byte` → **không sao, cứ chạy tiếp cell 7**. Cell 7 xoá chúng. Nhưng nếu cell 7 không xoá được (báo `GIU LAI`), gửi log cho tôi.

Dung lượng dùng khoảng 14 GB. Colab free có ~78 GB nên đủ.

> Script chỉ giải mã ảnh của những dòng thực sự được giữ lại, nên bước này nhanh hơn cách đọc toàn bộ dataset. Vẫn nên để chạy liền mạch, đừng bấm stop giữa chừng.

### Cell 7 — Xoá ảnh trùng byte (1–2 phút, **bắt buộc**)

```bash
python train_nutrivision.py dedupe --data-dir /content/data
```

**Vì sao bắt buộc:** Food-101 có ảnh trùng thật, và một số ảnh nằm ở hai split khác nhau. Nếu ảnh trong `test` cũng nằm trong `train` thì model đã "học thuộc" nó — điểm test sẽ đẹp giả, không dùng để báo cáo được.

Cách xử lý: nhóm các file giống nhau **từng byte**, rồi **giữ bản ở split ưu tiên `test` > `val` > `train`**, xoá phần còn lại. Giữ `test` vì đó là tập dùng để đo điểm; mất vài ảnh trong train không ảnh hưởng.

Kỳ vọng cuối log:
```text
[NutriVision] Trung byte: N nhom | da xoa M file | giu lai 0 file
[NutriVision] Dataset hop le: du 131 lop o ca 3 split.
[NutriVision] Khong co file trung byte.
```

Muốn xem trước sẽ xoá gì mà không xoá thật: thêm `--dry-run`.

> Nếu thấy `GIU LAI ... (xoa se lam rong lop)` → file đó là ảnh cuối cùng của một lớp, script không xoá để dataset còn đủ 131 lớp. Gửi log cho tôi, đừng tự xoá tay.

Ngoài ra `train` cũng tự chặn nếu còn ảnh trùng giữa hai split — nên nếu bỏ qua cell 7, cell 8/9 sẽ dừng ngay với dòng `LOI: ... nhom anh trung byte giua cac split`.

### Cell 8 — Smoke test (2–3 phút)

```bash
python train_nutrivision.py train --data-dir /content/data --out-dir /content/out_smoke --arch small --smoke
```

Mục đích: chạy 1 epoch ngắn để phát hiện lỗi shape, lỗi nhãn, lỗi export **trước khi** train 3 giờ.

Kỳ vọng:
- In ra `131 lop`.
- Có file `/content/out_smoke/model.tflite`.
- Không có traceback.

> Smoke chỉ để kiểm tra pipeline, **không dùng model này**.

### Cell 9 — Train thật (2–3 giờ)

```bash
python train_nutrivision.py train --data-dir /content/data --out-dir /content/out --arch large --epochs1 6 --epochs2 4
```

- Ghi ra `/content/out` (đĩa của Colab), **không ghi thẳng vào Google Drive**: ghi checkpoint qua Drive rất chậm và dễ làm treo phiên train. Cell 12 sẽ copy sang Drive sau.
- `--arch large`: chính xác hơn, model ~5–6 MB.
- Nếu muốn nhanh hơn một nửa: `--arch small`.
- Nếu Colab sắp hết giờ: giảm `--epochs1 4 --epochs2 2`, chấp nhận accuracy thấp hơn.

Kỳ vọng cuối log:
```text
[NutriVision] top-1 = 0.xxxx | top-3 = 0.xxxx
[NutriVision] model.tflite: x.xx MB
```

### Cell 10 — Verify model

```bash
python train_nutrivision.py verify --data-dir /content/data --out-dir /content/out
```

Kỳ vọng: in shape input/output, rồi top-3 của 10 ảnh test. Nếu top-1 dưới 5/10 → báo tôi, gần như chắc chắn sai thứ tự nhãn hoặc preprocessing.

### Cell 11 — Xem kết quả

Cell này in 3000 ký tự đầu của `classification_report.txt`, `top1`/`top3` từ `metrics.json`, và **bảng ngưỡng** từ `confidence_report.json`:

```text
nguong 0.70 | coverage 72.7% | dung khi tren nguong 89.7%
```

Đó là số dùng để chốt ngưỡng gọi Gemini — xem mục 5.6.

### Cell 12 — Copy sang Drive + tải kết quả

Cell này copy 9 file từ `/content/out` sang `MyDrive/NutriVision_AI`, báo `saved`/`MISSING` từng file, rồi nén và tải `nutrivision_model.zip`.

Nếu file nào báo `MISSING`, ghi lại tên file đó — thường là `confidence_hist.png` khi matplotlib lỗi, không ảnh hưởng model.

---

## 4. Đọc kết quả — đừng chỉ nhìn một con số

| File | Xem gì |
|---|---|
| `metrics.json` | `top1`, `top3`, số lớp, kích thước input, ngưỡng |
| `classification_report.txt` | Precision/recall/F1 **từng món** — tìm món yếu |
| `confusion_matrix.png` | Món nào bị nhầm với món nào |
| `confidence_report.json` | Phân bố `max(p)` và bảng ngưỡng — dùng để **chọn ngưỡng gọi Gemini** |
| `confidence_hist.png` | Ảnh đúng vs ảnh sai nằm ở dải tin cậy nào |
| `training_curves.png` | Overfit hay chưa (val tách xa train) |
| `labels.txt` | **Thứ tự lớp model xuất ra** — phải khớp app |

Cách đọc:
- `top1` = đoán đúng ngay lần đầu.
- `top3` = món đúng nằm trong 3 gợi ý.
- Nếu `top1` thấp nhưng `top3` cao → dùng UI top-3 là hợp lý.
- Nếu một món có recall rất thấp → thiếu ảnh hoặc nhãn gốc bị lẫn.

> **Không** ghi "độ chính xác 95%" vào báo cáo nếu `metrics.json` không ghi đúng như vậy. Số liệu phải lấy từ file.

### 4.1. Kết quả lần train 07/11/2026 (số thật, lấy từ `metrics.json`)

| Hạng mục | Số đo |
|---|---|
| Model | MobileNetV3-Large, input 224×224, dynamic-range int8, **3,28 MB** |
| Train / val / test | 24.549 / 9.813 / 9.814 ảnh |
| **Top-1** | **76,5 %** |
| **Top-3** | **89,8 %** |
| Test loss | 0,921 |

Kiểm tra file TFLite (`verify` trên Colab): input `[1 224 224 3]` float32, output `[1 131]` float32, top-1 **8/10**, top-3 **9/10** trên 10 ảnh test lấy ngẫu nhiên.

Overfit nhẹ: train acc 0,86 vs val 0,76; val gần như đứng yên từ epoch 6. Muốn tăng top-1 thì tăng `--max-train` (đang chặn 250 ảnh/lớp) hoặc `--epochs2`.

**12 món yếu nhất** (recall, tức tỉ lệ ảnh món đó được nhận đúng):

| Món | Recall | Số ảnh test |
|---|---|---|
| `banh_da_cua` | 0,375 | 22 |
| `banh_duc` | 0,457 | 97 |
| `luon_om_chuoi_dau` | 0,467 | 19 |
| `bun_bo_hue` | 0,521 | 226 |
| `thit_kho_tau` | 0,524 | 32 |
| `banh_la` | 0,526 | 19 |
| `cao_lau` | 0,541 | 94 |
| `banh_canh` | 0,560 | 141 |
| `banh_bot_loc` | 0,562 | 104 |
| `bun_cha` | 0,571 | 16 |
| `banh_hoi` | 0,583 | 180 |
| `luon_xao_xa_ot` | 0,600 | 24 |

`banh_da_cua`, `luon_om_chuoi_dau`, `banh_la`, `bun_cha` chỉ có 16–22 ảnh test → recall thấp một phần vì ít dữ liệu. Còn `bun_bo_hue` (226 ảnh) và `banh_canh` (141 ảnh) recall thấp là do **nhầm thật** với món khác — nhìn `confusion_matrix.png` để biết nhầm với món nào.

Chỉ 3/131 món có recall dưới 0,5. Không có món nào recall = 0.

---

## 5. Cách hiển thị kết quả và ngưỡng tin cậy

Model trả về **131 xác suất**. App lấy 3 món cao nhất và gọi là top-1, top-2, top-3.

### 5.1. Trên 70% — hiện top-1

```text
Phở                        72%
2 gợi ý khác: Bún bò Huế 9% · Hủ tiếu 5%
```

Người dùng vẫn bấm được "không phải món này" nếu sai.

### 5.2. Dưới 70% — hiện top-3 để chọn nhanh

Không đoán bừa một món. Hiện 3 món cao nhất kèm xác suất, người dùng bấm món đúng:

| Hạng | Món | Xác suất |
|---|---|---|
| Top 1 | Phở | 52% |
| Top 2 | Bún bò Huế | 30% |
| Top 3 | Hủ tiếu | 12% |

Ba con số này **không cộng lại thành 100%**: phần còn lại (6% trong ví dụ) chia cho 128 món khác. Đây là 3 món cao nhất trong **một lần** dự đoán, không phải 3 lần đoán độc lập.

> Lưu ý về tên hiển thị: lớp trong model là `pho` chung, tên hiện ra là **"Phở"**, không phải "Phở bò". Model không phân biệt phở bò / phở gà. Tên lấy từ `class_names_vi.json`.

### 5.3. Khi top-3 không có món đúng — gọi Gemini

```text
max(p) >= 0.70                      -> hiện top-1 + thanh tin cậy
max(p) <  0.70                      -> hiện top-3 + nút "Nhận diện bằng Gemini" + "Tìm món thủ công"
người dùng bấm "không phải món này"  -> gọi Gemini (kể cả khi p cao)
```

Lý do: với 131 lớp, model có thể "tự tin sai". Nhánh Gemini chỉ chạy khi thật sự cần, nên tiết kiệm thời gian và hạn mức API.

### 5.4. Quy ước khi gọi Gemini

1. **Chỉ gọi khi có mạng.** Không có mạng → giữ top-3 + tìm món thủ công, không báo lỗi đỏ.
2. **Chỉ gửi ảnh** (resize cạnh dài ~768 px) + một câu hỏi ngắn. Không gửi lịch sử bữa ăn, không gửi thông tin cá nhân.
3. **Gemini trả tên món tự do.** App xử lý 2 trường hợp:
   - Tên khớp một trong 131 id → dùng `foods_nutrition.json` local, coi như kết quả bình thường.
   - Tên **không** khớp 131 id → lưu thành mục "món ngoài danh mục", đánh dấu `source = "gemini"`. **Không** thêm id mới vào `labels.txt`, không đổi 131 lớp của model.
4. **Lưu kết quả vào Room** ngay khi có, để mở lại không phải gọi API lần nữa.
5. **Không để Gemini ghi đè kết quả model.** Tách nguồn: `source = "model"` / `"gemini"` / `"user"`.
6. **Không để Gemini điền calo.** Nó chỉ trả tên món. Dinh dưỡng lấy từ `foods_nutrition.json`; món ngoài danh mục thì hiện "chưa có dữ liệu dinh dưỡng" + nút để người dùng tự nhập. Nếu sau này có tra USDA thì lưu thêm `nutrition_source = "local" | "usda" | "user"`, **không bao giờ** là `"gemini"`.

### 5.5. Lưu gì khi người dùng bấm chọn

- Bấm một trong top-3 → lưu bữa ăn với `source = "model"`, `confirmed = true`, kèm `confidence` của món đã chọn.
- Bấm "không phải món này" rồi Gemini trả về → `source = "gemini"`, `confirmed = true`.
- Tự nhập tay → `source = "user"`.

Cột `confirmed` rất có ích về sau: lọc các ảnh `confirmed = true` để bổ sung dataset, hoặc đo tỉ lệ người dùng phải sửa kết quả model.

### 5.6. Ngưỡng đã chọn, dựa trên số thật

`confidence_report.json` của lần train 07/11/2026:

| Ngưỡng | % ảnh hiện top-1 | Đúng khi đã hiện top-1 | % tổng số ảnh bị hiện **sai** một món |
|---|---|---|---|
| 0,40 | 92,8 % | 80,5 % | 18,1 % |
| 0,50 | 86,7 % | 83,5 % | 14,3 % |
| **0,60** | **79,7 %** | **86,9 %** | **10,5 %** |
| **0,70** | **72,7 %** | **89,7 %** | **7,5 %** |
| 0,80 | 65,2 % | 92,5 % | 4,9 % |
| 0,90 | 55,7 % | 95,4 % | 2,6 % |

Đọc bảng này: ở ngưỡng 0,60, cứ 100 ảnh thì 80 ảnh được hiện top-1, trong đó 87 % đúng — nghĩa là **10,5 % tổng số ảnh bị app khẳng định sai một món**. Lên 0,70 thì con số đó còn 7,5 %, đổi lại 27 % ảnh phải hiện màn top-3 thay vì 20 %.

`confidence_hist.png` cho thấy model **hơi tự tin quá**: ảnh sai có trung vị `max(p)` = 0,57 và 10 % ảnh sai vẫn đạt trên 0,91. Vì vậy không có ngưỡng nào lọc sạch hoàn toàn — đây là lý do vẫn cần nút "không phải món này" và Gemini.

**Đã chốt: dùng 0,70** (không dùng 0,60). Lý do: giảm số ảnh bị khẳng định sai từ 10,5 % xuống 7,5 %, đổi lại thêm 7 % ảnh phải hiện màn top-3. Ngưỡng là tham số cấu hình (`model_meta.json`), đổi được mà không cần train lại — nếu ưu tiên "không bao giờ hiện sai" hơn số lần phải bấm chọn thì nâng lên 0,80.

- `labels.txt` và 131 id **phải ổn định**, vì bữa ăn đã lưu trong Room tham chiếu theo id. Đổi id = mất liên kết dữ liệu cũ.
- Muốn tăng top-1: tăng `--max-train` (đang 250 ảnh/lớp) hoặc `--epochs2`, chứ **không** hạ ngưỡng.

### 5.7. API key

Không nhúng API key Gemini trực tiếp trong APK (ai mở APK cũng đọc được). Cách đúng: gọi qua **Firebase Cloud Functions** (key nằm ở server) hoặc dùng key bị giới hạn theo package + SHA-1. Đây là việc Tuần 3, nhưng phải nhớ trước khi nộp.

---

## 6. Danh sách 131 lớp

- Bảng đầy đủ: `class_summary.csv` (mở bằng Excel).
- Nhóm Việt (101): phở, bún bò Huế, bún chả, bánh mì, cơm tấm, bánh xèo, gỏi cuốn, bánh cuốn, bún riêu, miến Quảng, bánh khọt, bánh bèo, chả cá Lã Vọng, bò lá lốt, bánh pía, xôi xéo…
- Nhóm ngoại (30): pizza, hamburger, hot dog, khoai tây chiên, mì Ý, lasagna, bít tết, tacos, salad Caesar, salad Hy Lạp, sushi, sashimi, ramen, bibimbap, pad Thái, vịt quay Bắc Kinh, donut, waffles, pancakes, cánh gà, cá và khoai tây chiên, nachos, hành tây chiên, nui phô mai, cá hồi nướng, sủi cảo, takoyaki, tiramisu, cheesecake.

### Hai điểm đã gộp

| Tên trong dataset | Gộp về | Lý do |
|---|---|---|
| `bo-la-lot` + `bo-nuong-la-lot` | `bo_la_lot` | Cùng một món |
| `banh-pia` + `nam-pia` | `banh_pia` | Cùng một món |

---

## 7. Trạng thái `app/src/main/assets/` và việc còn thiếu

**Đã đồng bộ xong (không sửa code Android):**

| File | Trạng thái |
|---|---|
| `labels.txt` | **131 dòng**, đúng thứ tự model, khớp `labels_131.txt` |
| `class_names_vi.json` | **131 id → tên tiếng Việt**, khớp model |
| `foods_nutrition.json` | **145 mục** = 131 lớp + 14 món cũ, **145/145 có số liệu dinh dưỡng** |
| `model.tflite` | **đã có** — 3,28 MB, header `TFL3`, top-1 76,5 % / top-3 89,8 % trên test |
| `model_meta.json` | **đã có** — input 224 float32 `[0,255]`, output 131 float32 |

Trong `foods_nutrition.json`:
- **105 mục** mang `category: "vietnamese"` (101 lớp + 4 món cũ), **40 mục** `"international"` (30 lớp + 10 món cũ).
- **145/145 mục có đủ số liệu** — không còn mục `null` nào.
- **131 mục** có `model_class: true` (model nhận diện được), **14 món cũ** `model_class: false` — app dùng cho tìm thủ công, không hiện trong top-1/top-3.
- Mỗi mục có thêm `source`, `method`, `confidence` và 6 trường `*_per_100g`.

Nguồn từng món, cách kiểm tra và giới hạn sai số: [`NGUON_DINH_DUONG.md`](NGUON_DINH_DUONG.md).

| `method` | Số mục | Nghĩa |
|---|---|---|
| `direct` | 4 | lấy nguyên từ USDA FoodData Central, có `fdcId` |
| `recipe` | 40 | cộng nguyên liệu theo gram rồi chia suất — ước lượng ±15–20 % |
| `estimate` | 101 | số nhập tay giai đoạn đầu, nhất quán nội bộ nhưng **chưa có bản ghi nguồn** |

- 14 món cũ (`pho_bo`, `pho_ga`, `xoi_ga`, `cha_gio`, `fried_chicken`, `burrito`, `udon`, `tempura`, `kimbap`, `tteokbokki`, `tom_yum`, `dimsum`, `croissant`, `milk_tea`) **không phải lớp của model** nhưng vẫn để lại để màn hình tìm món thủ công còn dữ liệu.

> File gốc trước khi sửa nằm ở `ai_tools/_archive/app_assets_backup/`.
> Muốn chạy lại: `python ai_tools/_source/sync_app_assets.py` (thêm `--check` để xem trước).
> Muốn dựng lại dinh dưỡng: `python ai_tools/_source/nutrition_build.py` (`--dry-run` để xem trước).

**Còn lại cho Tuần 3:**

1. ~~Điền số liệu cho 44 mục đang `null`~~ — **đã xong**, xem `NGUON_DINH_DUONG.md`.
2. **Nâng 101 mục `estimate` lên `direct`/`recipe`** nếu báo cáo cần trích dẫn nguồn cho từng món — hiện chúng chỉ nhất quán nội bộ.
3. **Đo latency thật** trên máy Android. Chưa có số đo thì không ghi "<50 ms". Model 3,28 MB nên chép vào APK không đáng ngại.
4. **Lớp Gemini fallback:** hàm gọi API, xử lý ảnh, map tên trả về với 131 id, lưu `source = "gemini"`. Gemini **không** được tự điền calo — món ngoài danh mục thì hiện "chưa có dữ liệu" + cho người dùng tự nhập (`nutrition_source = "user"`).
5. **Room + Firebase:** bảng bữa ăn có cột `source` (`model` / `gemini` / `user`) và `synced` (đã đẩy lên cloud hay chưa); đồng bộ khi có mạng, không chặn thao tác offline.
6. **Cloud Functions cho API key** (xem 5.7) nếu không muốn key nằm trong APK.
7. **Chụp thử ảnh thật** bằng điện thoại (không phải ảnh test của dataset) để kiểm tra top-1 ngoài đời — số 76,5 % là trên ảnh test cùng nguồn, ảnh tự chụp thường khó hơn.

---

## 8. Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `Thieu class_map.json` | Chưa chạy cell 4 | Chạy lại cell `%%writefile class_map.json` |
| `Lech nhan! Thieu: {...}` | Dataset thiếu lớp | Chạy lại `prepare`, kiểm tra log tải |
| `Dataset chua hop le` | Thiếu ảnh ở val/test | Xoá `/content/data` rồi prepare lại |
| `CANH BAO: ... nhom file trung byte` | Ảnh trùng thật trong Food-101, có ảnh nằm 2 split | Chạy cell 7 (`dedupe`) |
| `LOI: ... nhom anh trung byte giua cac split` | Chưa chạy `dedupe` | Chạy `python train_nutrivision.py dedupe --data-dir /content/data` |
| `ResourceExhaustedError` / OOM | Batch quá lớn | Giảm `--batch 16` |
| Colab ngắt giữa chừng | Hết phiên free | `/content` mất cả dataset lẫn model đang train; phải chạy lại từ cell 6. Kết quả đã copy sang Drive ở cell 12 thì vẫn còn |
| `model.export loi` | Keras/Drive | Script tự fallback sang `from_keras_model`; nếu vẫn lỗi, đặt `--out-dir /content/out` rồi copy |
| TFLite top-1 thấp bất thường | Sai thứ tự nhãn / preprocessing | Kiểm tra `labels.txt` cùng thứ tự folder; chạy `verify` |
| `FileNotFoundError: food101` | Lỗi mạng khi tải | Chạy lại cell prepare; HF cache tự resume |

---

## 9. Bản quyền & trích dẫn

- **VN dataset:** `jeandeanny/vn-food-combined-2026`, license **CC BY-NC-SA 4.0** (ghi rõ trên dataset card) — chỉ dùng cho học tập/nghiên cứu, phải ghi nguồn, sản phẩm phái sinh giữ cùng license.
- **Food-101:** dataset card ghi license **unknown**; ảnh gốc thuộc Foodspotting, ETH Zurich chỉ phát hành cho mục đích nghiên cứu. Dùng ngoài phạm vi học thuật phải thỏa thuận với chủ ảnh.
- Khi viết báo cáo, ghi rõ 2 nguồn trên ở mục Dataset. Không ghi "ảnh miễn phí bản quyền".

> Nếu sau này làm thương mại, phải thay dataset hoặc xin phép.

---

## 10. Tiêu chí nghiệm thu phần AI

- [x] `prepare` chạy xong, log xác nhận **đủ 131 lớp** ở train/val/test.
- [x] `dedupe` chạy xong, log báo **0 nhóm file trùng byte** và 0 file trùng giữa hai split.
- [x] Smoke test tạo được `model.tflite`.
- [x] Train xong có `metrics.json` với `top1` = **76,5 %**, `top3` = **89,8 %**.
- [x] Có `confidence_report.json` và bảng ngưỡng thật (xem 4.1 và 5.6).
- [x] **Chốt ngưỡng 0,70** trong `model_meta.json` (cả `nutrivision_model/` và `app/src/main/assets/`), không cần train lại.
- [x] `verify` chạy được: input `[1 224 224 3]` float32, output `[1 131]` float32, top-1 **8/10**, top-3 **9/10**.
- [x] `labels.txt` có đúng 131 dòng, khớp `class_names_vi.json` và `assets/labels.txt`.
- [x] `app/src/main/assets/` có `labels.txt` 131 dòng, `class_names_vi.json` 131 id, `model.tflite` (3,28 MB) + `model_meta.json`.
- [x] File model < 10 MB.
- [x] Đã lưu model + báo cáo vào Google Drive.
- [x] `foods_nutrition.json`: **145/145 mục có đủ** calo/đạm/carb/béo/xơ/natri cho một khẩu phần gram cụ thể, kèm `source` / `method` / `confidence` và 6 trường `*_per_100g`.
- [x] Validator tự động chạy khi build: 0 lỗi (calo khớp macro, xơ ≤ carb, khẩu phần và kcal/100 g trong khoảng hợp lý, `*_per_100g` khớp lại số của khẩu phần).
- [x] `foods_nutrition.json` trong `_source/` và trong `app/src/main/assets/` **giống từng byte**.
- [x] Bảng nguồn từng món + giới hạn sai số: [`NGUON_DINH_DUONG.md`](NGUON_DINH_DUONG.md).

Khi tất cả ô trên được tick, phần AI coi như xong và có thể quay lại Tuần 3.
