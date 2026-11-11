# NÊN TRAIN MODEL HAY GỌI API? — Phân tích & phương án chốt

> **Kết luận 1 dòng:** **Train model (TFLite) là đường chính**, API chỉ là "lưới an toàn" tuỳ chọn. Nhóm 3 người, 6 tuần, demo trước lớp → train thắng API ở mọi tiêu chí trừ "công sức ban đầu".

---

## 1. Bảng so sánh 3 phương án

| Tiêu chí | **A. Train TFLite** (Teachable Machine) | **B. Gọi API** (Gemini / Cloud Vision / LogMeal) | **C. Không AI** (chỉ tìm kiếm) |
|---|---|---|---|
| Nhận diện món Việt | Tốt **với đúng 15 món đã train** (~85–95%) | Khá–tốt (Gemini, GPT-4o), nhưng không đoán được món hiếm | Không có |
| **Chạy offline** | ✅ Có | ❌ Không | ✅ Có |
| Tốc độ | **~50–150 ms** | 1–3 s + chờ mạng | – |
| Chi phí | 0đ | API key + quota (Gemini free tier / trả phí) | 0đ |
| Rủi ro khi demo | Thấp (đã test trước) | **Cao**: mất mạng, hết quota, key bị chặn, nhà trường chặn wifi | Không |
| Công sức ban đầu | Thu ảnh + train: ~2–3 ngày rải rác | ~1 ngày (đăng ký, viết service, xử lý lỗi) | 0 |
| Độ "ăn điểm" kỹ thuật | **Cao**: có pipeline AI thật, có số liệu accuracy | Trung bình: chỉ là gọi dịch vụ | Thấp |
| Bảo mật | Không lộ gì | **Lộ API key** nếu nhúng trong APK | – |
| Hiểu bài để trả lời hội đồng | Trả lời được (confusion matrix, augmentation) | Chỉ trả lời được "em gọi API" | – |

**Điểm quan trọng nhất:** đề bài tuần 4 ghi *"gọi API (nếu có)"* → API **không bắt buộc**. Nhưng "nhận diện ảnh" là **linh hồn của đề tài** → không thể giao cho một thứ mình không kiểm soát được (API + mạng + quota).

---

## 2. Vì sao API không phù hợp làm đường chính

1. **Món Việt là điểm mù của API nước ngoài.** Cloud Vision trả nhãn chung chung ("Food", "Dish", "Cuisine"), LogMeal/Clarifai thiên món Âu. Gemini/GPT-4o nhận ra "phở" khá tốt nhưng:
   - Không trả về **khẩu phần**, chỉ nói tên món.
   - Có thể **bịa** (hallucination) số calo.
   - **1–3 giây/ảnh** → demo 10 lần chụp là 30 giây chờ.
2. **Phụ thuộc hạ tầng ngoài.** Wifi trường yếu/chặn, hết quota tháng, key hết hạn → demo chết ngay trước hội đồng. Đây là kiểu tai nạn rất hay gặp.
3. **Lộ API key.** Nhúng key trong APK = ai cũng lấy được (decompile 30 giây). Muốn đúng chuẩn phải có backend trung gian → vượt phạm vi môn học.
4. **Không chứng minh được năng lực môn học.** Hội đồng hỏi "em làm gì trong phần AI?" → "em gọi API" là hết chuyện.

---

## 3. Vì sao train lại vừa sức hơn nhóm nghĩ

Teachable Machine (của Google) đã làm hộ 90% việc khó: không cần biết Python, không cần GPU, không cần cài TensorFlow.

| Bước | Thời gian thực tế |
|---|---|
| Thu 15 lớp × 80 ảnh (chia 3 người) | 2 buổi tối (~6 giờ) |
| Upload + train trên Teachable Machine | **10–15 phút** (bấm nút) |
| Export TFLite + đưa vào `assets/` | 15 phút |
| Viết class `TfliteFoodRecognizer` | 2–3 giờ |
| Test + tinh chỉnh ngưỡng confidence | 3–4 giờ |
| **Tổng** | **~2 ngày người, rải trong tuần 2–3** |

Và nhóm có **model của riêng mình**, chạy offline, nhẹ 2–5 MB, demo không cần mạng.

**Con số để đưa vào báo cáo:** 15 lớp × ~80 ảnh ≈ 1.200 ảnh → accuracy tập test thường đạt **85–93%** nếu ảnh sạch và cân bằng lớp. Đây là con số rất "đẹp" để trình bày.

---

## 4. Phương án chốt: **HYBRID** — train là chính, API là phụ

Kiến trúc này vừa an toàn vừa ghi điểm kiến trúc (hội đồng rất thích interface + đa implementation):

```
                    ┌──────────────────────────┐
   Ảnh món ăn  ───► │   FoodRecognizer (interface) │
                    └───────────┬──────────────┘
                                │
             ┌──────────────────┴───────────────────┐
             ▼                                      ▼
  ┌─────────────────────┐              ┌──────────────────────────┐
  │ TfliteRecognizer    │              │ ApiRecognizer (tuỳ chọn) │
  │ • offline, ~100ms   │              │ • chỉ khi top1 < 0.6     │
  │ • 15 lớp món Việt   │              │ • Gemini / Vision        │
  └──────────┬──────────┘              └────────────┬─────────────┘
             │                                      │
             └──────────────┬───────────────────────┘
                            ▼
              ┌──────────────────────────────┐
              │ Xác nhận / sửa món (bắt buộc)│  ← van an toàn cuối cùng
              └──────────────┬───────────────┘
                             ▼
              ┌──────────────────────────────┐
              │ Tra DB local → tính khẩu phần│
              └──────────────────────────────┘
```

**Quy tắc định tuyến (routing rule):**

| Trường hợp | Hành động |
|---|---|
| top1 ≥ 0.80 | Gợi ý món đó, vẫn cho sửa |
| 0.60 ≤ top1 < 0.80 | Hiện **top-3** để người dùng chọn |
| top1 < 0.60 | Hiện top-3 + ô tìm kiếm; **nếu bật API** thì gọi API song song |
| Mất mạng / API lỗi | Im lặng fallback về tìm kiếm thủ công (không báo lỗi đỏ) |

> Nếu **hết thời gian**, bỏ `ApiRecognizer` — hệ thống vẫn chạy đủ. Nếu **còn thời gian**, thêm nó ở tuần 5 như "chức năng mở rộng E3". Không bao giờ làm ngược lại.

---

## 5. Quy trình train chi tiết (6 bước)

### Bước 1 — Chuẩn bị thư mục ảnh
```
dataset/
├── pho_bo/        (80–120 ảnh)
├── pho_ga/
├── bun_bo_hue/
├── bun_cha/
├── bun_rieu/
├── com_tam/
├── com_trang/
├── banh_mi/
├── goi_cuon/
├── cha_gio/
├── banh_xeo/
├── mi_quang/
├── rau_xao/
├── trung_luoc/
└── trai_cay/
```
Yêu cầu ảnh: đa dạng góc, ánh sáng, bát đĩa, nền; **không** ảnh watermark; mỗi lớp chênh nhau ≤ 20%.

### Bước 2 — Train
1. Vào `teachablemachine.withgoogle.com` → **Image Project** → **Standard image model (224×224)**.
2. Tạo 15 class, upload đúng thư mục.
3. **Advanced → Epochs: 20–30**, Batch size 16, Learning rate mặc định.
4. Bấm **Train Model** (~10 phút).
5. Kiểm tra bằng **webcam/ảnh upload**: cố tình đưa ảnh sai để xem model có "tự tin sai" không.

### Bước 3 — Export
- **TensorFlow Lite** → ưu tiên bản **Float32/Float16** (tránh Quantized để không phải dequantize output).
- Tải về: `nutrivision_food.tflite` + `labels.txt` (đúng thứ tự class!).

### Bước 4 — Đưa vào project
```
app/src/main/assets/nutrivision_food.tflite
app/src/main/assets/labels.txt
```
`build.gradle`:
```gradle
implementation "org.tensorflow:tensorflow-lite:2.16.1"
implementation "org.tensorflow:tensorflow-lite-support:0.4.4"
android { aaptOptions { noCompress "tflite" } }
```

### Bước 5 — Code (đã có sẵn trong mục 6)

### Bước 6 — Đo & ghi lại số liệu (để đưa vào báo cáo)
- Accuracy trên 10% ảnh test giữ riêng.
- Thời gian inference trung bình (ms) trên máy thật.
- Kích thước model (MB).
- 3 ảnh **model đoán sai** + phân tích nguyên nhân (ánh sáng vàng, món bị che, góc chụp lạ) → phần "hạn chế" của báo cáo sẽ rất thuyết phục.

---

## 6. Code mẫu

### 6.1 Interface (điểm cộng kiến trúc)

```kotlin
data class Prediction(val label: String, val confidence: Float)

interface FoodRecognizer {
    suspend fun recognize(bitmap: Bitmap): List<Prediction>  // đã sort giảm dần
}
```

### 6.2 TFLite implementation

```kotlin
class TfliteFoodRecognizer(
    private val context: Context,
    private val modelName: String = "nutrivision_food.tflite",
    private val labelsName: String = "labels.txt"
) : FoodRecognizer {

    private val labels: List<String> by lazy {
        context.assets.open(labelsName).bufferedReader().useLines { it.toList() }
            .filter { it.isNotBlank() }
    }

    private val interpreter: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, modelName)
        Interpreter(model, Interpreter.Options().apply { setNumThreads(4) })
    }

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))   // (x - 127.5) / 127.5  -> [-1, 1]
        .build()

    override suspend fun recognize(bitmap: Bitmap): List<Prediction> =
        withContext(Dispatchers.Default) {
            val tensor = TensorImage(DataType.FLOAT32).apply { load(bitmap) }
            val processed = imageProcessor.process(tensor)

            val output = Array(1) { FloatArray(labels.size) }
            interpreter.run(processed.buffer, output)

            output[0]
                .mapIndexed { i, score -> Prediction(labels[i], score) }
                .sortedByDescending { it.confidence }
                .take(3)
        }
}
```

> ⚠️ **Kiểm tra chuẩn hóa:** Teachable Machine thường dùng `[-1, 1]` (như trên). Nếu chạy 5 ảnh test mà kết quả vô lý (confidence gần như đều nhau), đổi `NormalizeOp(127.5f, 127.5f)` → `NormalizeOp(0f, 255f)` (tức `[0, 1]`) và test lại. Chỉ mất 5 phút.

### 6.3 API implementation (tuỳ chọn, làm sau)

```kotlin
class GeminiFoodRecognizer(
    private val api: GeminiApi,
    private val apiKey: String
) : FoodRecognizer {

    override suspend fun recognize(bitmap: Bitmap): List<Prediction> = try {
        val base64 = bitmap.toBase64Jpeg(quality = 70)
        val prompt = """
            Bạn là chuyên gia dinh dưỡng Việt Nam. Nhận diện món ăn chính trong ảnh.
            Chỉ trả về JSON: {"dishes":[{"name":"...","confidence":0.0}]}
            Tên món dùng tiếng Việt, tối đa 3 món, không giải thích.
        """.trimIndent()

        api.generate(apiKey, prompt, base64)
            .map { Prediction(it.name, it.confidence) }
    } catch (e: Exception) {
        emptyList()          // lỗi mạng/quota -> im lặng fallback
    }
}
```

### 6.4 Chọn implementation theo cấu hình

```kotlin
class FoodRecognizerProvider(
    private val local: TfliteFoodRecognizer,
    private val remote: GeminiFoodRecognizer?   // null nếu chưa bật
) {
    suspend fun recognize(bitmap: Bitmap, useApiFallback: Boolean): List<Prediction> {
        val localResult = local.recognize(bitmap)
        val confident = localResult.firstOrNull()?.confidence ?: 0f
        return if (useApiFallback && confident < 0.6f && remote != null) {
            remote.recognize(bitmap).ifEmpty { localResult }
        } else localResult
    }
}
```

---

## 7. Lịch làm AI (không được trễ)

| Tuần | Việc | Ai |
|---|---|---|
| 1 | Chốt 15 nhãn, tạo thư mục Drive, bắt đầu thu ảnh | Cả 3 |
| 2 | Đủ ảnh (mỗi lớp ≥ 50), train bản thử số 1 | TV1 |
| 3 | Train bản chính thức, export TFLite, viết `TfliteFoodRecognizer`, chạy được trên máy thật | TV1 |
| 4 | Tinh chỉnh ngưỡng confidence, xử lý ảnh xấu, đo thời gian inference | TV1 |
| 5 | (Nếu còn thời gian) thêm Gemini fallback + so sánh trong báo cáo | TV1 + TV2 |
| 6 | Chuẩn bị ảnh demo **đã test trước**, quay video dự phòng | Cả 3 |

---

## 8. Câu hỏi hội đồng sẽ hỏi & câu trả lời

| Câu hỏi | Trả lời |
|---|---|
| "Sao không dùng API cho nhanh?" | "API mạnh nhưng phụ thuộc mạng, tốn phí và không có dữ liệu khẩu phần món Việt. Nhóm chọn model on-device để app chạy offline và phản hồi ~100ms; API được giữ làm fallback cho món ngoài 15 lớp." |
| "Model nhận diện sai thì sao?" | "AI chỉ đưa ra top-3 gợi ý, người dùng luôn xác nhận hoặc sửa. Sai ở bước AI không làm sai dữ liệu dinh dưỡng." |
| "Sao chỉ 15 món?" | "15 món phủ nhóm phổ biến nhất; kiến trúc cho phép thêm lớp bằng cách bổ sung ảnh và train lại, không đổi code." |
| "Accuracy bao nhiêu, đo thế nào?" | Trình bày số đo trên tập test giữ riêng + confusion matrix (Teachable Machine xuất được). |
| "Sao không tự viết model?" | "Đề tài tập trung vào ứng dụng di động; nhóm dùng transfer learning qua Teachable Machine để tối ưu thời gian, và hiểu rõ pipeline tiền xử lý → inference → hậu xử lý." |

---

## 9. Nếu... (phương án dự phòng)

| Tình huống | Xử lý |
|---|---|
| Tuần 3 model vẫn kém (< 70%) | Giảm còn **8 lớp dễ** (phở bò, cơm trắng, bánh mì, gỏi cuốn, trứng, trái cây, chả giò, bún chả) → accuracy tăng rõ rệt |
| Không kịp thu ảnh | Bổ sung ảnh từ Pexels/Unsplash (ghi nguồn), ưu tiên ảnh chụp thật |
| Model chạy chậm trên máy yếu | Giảm input 160×160, dùng bản Float16, `setNumThreads(4)` |
| AI hỏng hoàn toàn trước demo | Tắt cờ `enableAi`, app chuyển sang tìm kiếm thủ công — **vẫn đủ 90% điểm** vì 13 chức năng khác vẫn chạy |
