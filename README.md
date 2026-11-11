# NutriVision

Ứng dụng Android nhận diện món ăn từ ảnh và ước tính dinh dưỡng (đồ án Lập trình thiết bị di động).

## Kiến trúc chốt

| Lớp | Công nghệ | Vai trò |
|---|---|---|
| Nhận diện on-device | MobileNetV3 + TFLite, **131 lớp** | Nhận diện món quen, không cần mạng |
| Fallback | **Gemini API** | Món lạ hoặc model không chắc — cần internet |
| Lưu trữ | **Room / SQLite** | Nguồn dữ liệu chính trên máy |
| Đồng bộ | **Firebase** | Đẩy/lấy dữ liệu khi có mạng |

App **không offline hoàn toàn**:

- Nhận diện 131 món và xem lại bữa ăn đã lưu: chạy offline.
- Gemini fallback và đồng bộ Firebase: cần internet, không được chặn thao tác offline.

Luồng quyết định:

```text
max(p) >= 0.70  -> hiện top-1 + thanh tin cậy
max(p) <  0.70  -> hiện top-3 kèm % để người dùng bấm chọn
                   (ví dụ: Phở 52% · Bún bò Huế 30% · Hủ tiếu 12%)
bấm "không phải món này" -> gọi Gemini
```

Top-3 là **3 món có xác suất cao nhất trong cùng một lần dự đoán**, không phải 3 lần đoán độc lập; 3 con số không cộng lại thành 100%.

## Phần AI

- Hướng dẫn chạy: [`ai_tools/HUONG_DAN_AI_131MON.md`](ai_tools/HUONG_DAN_AI_131MON.md)
- Notebook Colab: [`ai_tools/Train_NutriVision_131.ipynb`](ai_tools/Train_NutriVision_131.ipynb)
- 131 lớp = 101 món Việt (Hugging Face `jeandeanny/vn-food-combined-2026`) + 30 món ngoại (`ethz/food101`).
- Model đã train xong: MobileNetV3-Large, `model.tflite` **3,28 MB**, đặt tại `app/src/main/assets/` cùng `labels.txt`, `class_names_vi.json`, `foods_nutrition.json`, `model_meta.json`.
- Số đo trên test set (9.814 ảnh, từ `metrics.json`): **top-1 76,5 % · top-3 89,8 %**. Đây là ảnh cùng nguồn dataset, không phải ảnh tự chụp ngoài đời.
- Dữ liệu dinh dưỡng: **145/145 mục có đủ** calo, đạm, carb, béo, xơ, natri cho một khẩu phần gram cụ thể. Mỗi mục ghi rõ nguồn (`direct` / `recipe` / `estimate`) và có sẵn `*_per_100g` để tính lại khi đổi khẩu phần.
  Bảng nguồn từng món và giới hạn sai số: [`ai_tools/NGUON_DINH_DUONG.md`](ai_tools/NGUON_DINH_DUONG.md).

## Ghi chú

- `labels_131.txt` và 131 id phải **giữ nguyên** sau khi train, vì dữ liệu bữa ăn đã lưu tham chiếu theo id.
- Không nhúng API key Gemini vào APK; dùng Cloud Functions hoặc key giới hạn theo package.
- Không ghi số liệu độ chính xác/latency khi chưa có file đo thật.
- **Không để Gemini tự điền calo.** Gemini chỉ trả tên món; dinh dưỡng lấy từ `foods_nutrition.json`. Món ngoài danh mục thì hiện "chưa có dữ liệu" và cho người dùng tự nhập.
- Số dinh dưỡng chỉ đúng với **khẩu phần ghi kèm**. Kéo khẩu phần thì app nhân theo tỉ lệ — đây là xấp xỉ, không phải phép đo.
