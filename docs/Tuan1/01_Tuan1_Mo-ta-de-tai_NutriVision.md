# TUẦN 1 – MÔ TẢ ĐỀ TÀI

**Môn:** Lập trình di động
**Nhóm:** 3 thành viên
**GVHD:** Lê Minh Trí
**Ngày nộp:** ....../....../2026

---

## 1. Kết luận nhanh: đề tài có làm được không?

**CÓ – nhưng phải chọn đúng hướng kỹ thuật.** Đây là đề tài tốt vì:

- Có "điểm nhấn" công nghệ (AI thị giác máy tính) nên dễ ghi điểm khi demo.
- Có câu chuyện người dùng rõ ràng (ăn uống lành mạnh), dễ thuyết phục hội đồng.
- Đủ lớn để làm 6 tuần nhưng **vẫn cắt nhỏ được** thành các chức năng chạy được.

**Rủi ro lớn nhất cần tránh ngay từ tuần 1:**

| Rủi ro | Vì sao nguy hiểm | Cách xử lý (đã đưa vào phạm vi) |
|---|---|---|
| Model nhận diện không biết món Việt (phở, bún chả, cơm tấm...) | Các model có sẵn như Food-101, ML Kit chỉ nhận ~400 nhãn chung, gần như không có món Việt → demo "sai be bét" | **Tự train model nhỏ bằng Teachable Machine** trên 10–15 món Việt phổ biến, export TFLite chạy offline |
| Nhận diện sai 1 chút là cả app sai | Người dùng mất niềm tin, demo rủi ro | Luôn có bước **"Xác nhận / sửa lại món ăn"** sau khi AI đoán (top-3 gợi ý) |
| Không có dữ liệu dinh dưỡng món Việt | API nước ngoài không có phở, bún bò | **Xây DB nội bộ ~100–150 món Việt** (seed sẵn trong app), API chỉ là kênh bổ sung |
| Phụ thuộc API trả phí / mất mạng | Hết quota, mất mạng là chết app | App **chạy offline hoàn toàn** với DB local, API là tính năng "mở rộng" |

> **Tóm lại:** Nếu nhóm chọn hướng *"AI nhận diện → người dùng xác nhận → tra DB dinh dưỡng local theo khẩu phần"* thì đề tài vừa sức, demo ổn định, vẫn rất "ăn điểm". Nếu chọn hướng *"gọi API nhận diện món ăn rồi tin tuyệt đối"* thì gần như chắc chắn thất bại.

---

## 2. Tên đề tài

**Tên chính thức (khuyến nghị):**

> ### NutriVision – Ứng dụng phân tích dinh dưỡng món ăn từ hình ảnh

**Tên đầy đủ để ghi trên bìa báo cáo / slide:**

> **NutriVision: Xây dựng ứng dụng Android phân tích dinh dưỡng bữa ăn Việt Nam từ hình ảnh bằng học sâu trên thiết bị**

**Slogan (dùng cho slide/icon):** *"Chụp một bức ảnh – biết ngay bữa ăn của bạn"*

**Các phương án tên khác (nếu muốn đổi):**

| Tên | Ý nghĩa | Nhận xét |
|---|---|---|
| NutriVision | Nutrition + Vision (thị giác) | Đã quen, dễ đọc, phù hợp nhất |
| NutriLens | "Ống kính dinh dưỡng" | Hay, gợi hình ảnh camera, hơi lạ với người Việt |
| SnapEat | "Chụp và ăn" | Trẻ trung nhưng không nói lên dinh dưỡng |
| CaloSight | Calo + tầm nhìn | Dễ nhầm với app đếm calo thông thường |

→ **Giữ "NutriVision"**, thêm phụ đề tiếng Việt để hội đồng hiểu ngay.

---

## 3. Lý do chọn đề tài

1. **Xuất phát từ nhu cầu thật:** tỉ lệ thừa cân, béo phì và bệnh không lây nhiễm (tiểu đường, huyết áp, mỡ máu) ở người trẻ Việt Nam đang tăng. Nhiều người muốn ăn uống lành mạnh nhưng **không biết món mình ăn có bao nhiêu calo**.
2. **Điểm đau cụ thể của các app hiện có:** muốn ghi lại bữa ăn phải **gõ tên món, chọn khẩu phần, chọn đơn vị** — mất 30–60 giây/bữa, làm vài ngày là bỏ. Chụp ảnh chỉ mất 3–5 giây.
3. **Khoảng trống về dữ liệu Việt Nam:** các app nổi tiếng (MyFitnessPal, Yazio, Foodvisor) có hàng triệu món nhưng **rất yếu với món Việt** (phở, bún riêu, bánh mì, cơm tấm, gỏi cuốn...) hoặc tên dịch máy khó hiểu.
4. **Phù hợp mục tiêu môn học:** vận dụng được gần như toàn bộ kiến thức của môn Lập trình di động: UI, camera, xử lý ảnh, local database (Room/SQLite), gọi API, notification, phân quyền, đóng gói APK.
5. **Tính mở rộng cao:** từ bản MVP có thể phát triển thành trợ lý dinh dưỡng cá nhân hóa (gợi ý thực đơn, chatbot, wearable...).

---

## 4. Mục đích của ứng dụng

**Mục đích tổng quát:** Giúp người dùng Việt Nam **kiểm soát dinh dưỡng hằng ngày một cách nhanh nhất, ít thao tác nhất**, bằng cách biến một bức ảnh chụp bữa ăn thành dữ liệu dinh dưỡng có thể theo dõi được.

**Mục tiêu cụ thể (đo lường được):**

| # | Mục tiêu | Chỉ số |
|---|---|---|
| M1 | Ghi nhận một bữa ăn nhanh | ≤ 15 giây/bữa (chụp → xác nhận → lưu) |
| M2 | Nhận diện đúng món trong top-3 | ≥ 80% với 10–15 món đã huấn luyện |
| M3 | Có dữ liệu dinh dưỡng món Việt | ≥ 100 món, đủ 6 chỉ số: calo, protein, carb, fat, xơ, natri |
| M4 | Theo dõi được tiến độ | Biểu đồ 7 ngày / 30 ngày + so sánh với mục tiêu |
| M5 | Hoạt động không cần mạng | 100% chức năng cốt lõi offline |

---

## 5. Ứng dụng phục vụ ai? (Đối tượng người dùng)

### 5.1 Người dùng chính (Primary personas)

**Persona 1 – Sinh viên / nhân viên văn phòng muốn giảm cân (chiếm ~60%)**
- 18–30 tuổi, ăn ngoài 2–3 bữa/ngày, dùng điện thoại Android tầm trung.
- Muốn giảm 3–5 kg nhưng lười ghi chép, bỏ cuộc sau 1 tuần.
- Cần: ghi bữa ăn siêu nhanh, biết hôm nay còn bao nhiêu calo.

**Persona 2 – Người tập luyện (gym, chạy bộ) (~25%)**
- Cần kiểm soát **protein** và tổng calo theo mục tiêu tăng/giảm cân.
- Cần: xem nhanh macro của món, thống kê theo tuần.

**Persona 3 – Người có bệnh lý cần kiểm soát chế độ ăn (~15%)**
- Tiểu đường type 2, huyết áp, mỡ máu; người chăm sóc người thân.
- Cần: biết lượng đường, muối (natri) của món; cảnh báo món vượt ngưỡng.

### 5.2 Người dùng phụ (Secondary actors)

- **Quản trị viên nội dung** (nhóm tự dùng công cụ/CSV): thêm – sửa món ăn và dữ liệu dinh dưỡng.
- **Hệ thống AI (TFLite)** và **API dinh dưỡng bên ngoài**: actor phụ của hệ thống.

---

## 6. Ứng dụng giải quyết vấn đề gì?

| # | Vấn đề của người dùng | Cách NutriVision giải quyết |
|---|---|---|
| P1 | Ghi nhật ký ăn uống thủ công quá mất thời gian → bỏ cuộc sau vài ngày | Chụp ảnh → AI gợi ý món → xác nhận 1 chạm. Giảm từ ~60s xuống ~10s |
| P2 | Không biết món Việt mình ăn bao nhiêu calo / đạm / đường | DB 100+ món Việt với dữ liệu trên 100g + quy đổi theo khẩu phần thực tế (bát, tô, đĩa, cái) |
| P3 | App nước ngoài không có món Việt, tên khó hiểu, không có tiếng Việt | Ứng dụng **Việt hóa hoàn toàn**, dữ liệu món Việt là trung tâm |
| P4 | Ăn xong không biết hôm nay đã ăn bao nhiêu so với mục tiêu | Dashboard vòng tròn calo + thanh macro theo mục tiêu cá nhân (TDEE) |
| P5 | Không thấy tiến bộ nên mất động lực | Biểu đồ 7/30 ngày, chuỗi ngày ghi chép liên tiếp (streak) |
| P6 | Hay quên ghi bữa ăn | Nhắc nhở theo giờ (local notification, không cần server) |
| P7 | Ước lượng khẩu phần sai (1 tô phở to gấp đôi tô nhỏ) | Cho chọn khẩu phần theo **đơn vị thực tế** (nửa tô / 1 tô / tô lớn, gram) và quy đổi tự động |

**Giá trị khác biệt (USP) của nhóm:**
1. **Nhanh hơn:** ảnh thay vì gõ.
2. **Việt hơn:** món Việt + khẩu phần Việt + giao diện tiếng Việt.
3. **Riêng tư & offline:** ảnh và nhật ký lưu trên máy, không cần tài khoản trả phí, không cần mạng.

---

## 7. Tìm hiểu 2–3 ứng dụng tương tự và so sánh

### 7.1 MyFitnessPal (Mỹ) – "ông lớn" của đếm calo

- **Ưu:** DB thực phẩm khổng lồ (hàng chục triệu mục), quét mã vạch rất mạnh, cộng đồng lớn, biểu đồ chi tiết, tích hợp nhiều thiết bị.
- **Nhược:** món Việt nghèo nàn và thường là bản dịch máy; **tính năng nhận diện ảnh nằm trong gói Premium trả phí**; giao diện nặng, nhiều quảng cáo; phải gõ tay rất nhiều; chưa tối ưu tiếng Việt.
- **Bài học cho nhóm:** DB mạnh là yếu tố sống còn → nhóm phải đầu tư **dữ liệu món Việt**.

### 7.2 Foodvisor (Pháp) – app "chụp ảnh là ra calo" nổi tiếng nhất

- **Ưu:** nhận diện ảnh món ăn + **ước lượng khẩu phần tự động** khá tốt; trải nghiệm rất mượt; tư vấn dinh dưỡng có chuyên gia; hình ảnh đẹp.
- **Nhược:** **trả phí gần như toàn bộ**; chỉ mạnh món Âu/Mỹ, món Việt gần như không có; cần mạng; dữ liệu cá nhân đưa lên cloud.
- **Bài học cho nhóm:** đây chính là "hình mẫu" của NutriVision, nhưng nhóm **không nên** cố làm ước lượng khẩu phần tự động bằng AI (quá khó trong 6 tuần) → thay bằng **chọn khẩu phần thủ công** (đủ tốt, chính xác hơn, dễ demo).

### 7.3 Calorie Mama / LogMeal (nhận diện món ăn bằng API)

- **Ưu:** nhận diện ảnh món ăn, trả về nhãn + dinh dưỡng ước lượng; có API cho lập trình viên; hỗ trợ đa món trong 1 ảnh.
- **Nhược:** API **trả phí / giới hạn quota**, phụ thuộc mạng, độ chính xác với món Việt thấp, kết quả trả về là món Tây; không phù hợp đồ án sinh viên (chi phí + rủi ro demo).
- **Bài học cho nhóm:** dùng API chỉ như **kênh bổ sung (tuần 4, nếu còn thời gian)**, không đặt cược vào nó.

### 7.4 Bảng so sánh tổng hợp

| Tiêu chí | MyFitnessPal | Foodvisor | Calorie Mama / LogMeal | **NutriVision (nhóm)** |
|---|---|---|---|---|
| Nhận diện từ ảnh | Có (Premium) | Có (tốt) | Có (API) | **Có, offline (TFLite)** |
| Dữ liệu món Việt | Rất ít | Gần như không | Không | **Trọng tâm (100+ món)** |
| Ước lượng khẩu phần | Thủ công | Tự động (AI) | Ước lượng | **Thủ công theo đơn vị Việt** |
| Tiếng Việt | Kém | Không | Không | **Hoàn toàn** |
| Offline | Một phần | Không | Không | **Có (cốt lõi)** |
| Chi phí | Miễn phí + Premium | Trả phí | Trả phí | **Miễn phí** |
| Quyền riêng tư | Cloud | Cloud | Cloud | **Lưu máy** |
| Phù hợp đồ án 6 tuần | – | – | – | **Có (phạm vi đã cắt)** |

**Kết luận định vị:** NutriVision **không cạnh tranh về số lượng món** với MyFitnessPal, mà cạnh tranh về **độ nhanh + độ Việt + offline**. Đây là câu trả lời chuẩn khi hội đồng hỏi "app đã có nhiều rồi, sao làm nữa?".

---

## 8. Danh sách chức năng

### 8.1 Chức năng chính (bắt buộc – phải chạy được ở tuần 4)

| ID | Chức năng | Mô tả ngắn | Ưu tiên |
|---|---|---|---|
| F1 | Đăng ký tài khoản | Email + mật khẩu, kiểm tra định dạng, mật khẩu ≥ 6 ký tự, xác nhận mật khẩu | Cao |
| F2 | Đăng nhập / Đăng xuất | Email + mật khẩu, ghi nhớ phiên, quên mật khẩu (gửi mail) | Cao |
| F3 | Hồ sơ & mục tiêu cá nhân | Tuổi, giới, chiều cao, cân nặng, mức vận động → tính **BMI + TDEE** (Mifflin–St Jeor) → mục tiêu calo/ngày | Cao |
| F4 | Chụp / chọn ảnh món ăn | CameraX hoặc thư viện ảnh, nén ảnh trước khi xử lý | Cao |
| F5 | Nhận diện món ăn từ ảnh | Model TFLite (MobileNet fine-tune) trả về **top-3 món + độ tin cậy** | Cao |
| F6 | Xác nhận / sửa món | Người dùng chọn đúng món trong top-3 hoặc tìm tay; đây là "van an toàn" của AI | Cao |
| F7 | Chọn khẩu phần | Chọn đơn vị thực tế (nửa tô / 1 tô / tô lớn, cái, gram) → tính lại dinh dưỡng | Cao |
| F8 | Xem chi tiết dinh dưỡng | Calo, protein, carb, fat, chất xơ, đường, natri; cảnh báo nếu vượt ngưỡng | Cao |
| F9 | Ghi nhật ký bữa ăn | Thêm vào bữa Sáng/Trưa/Tối/Snack theo ngày; **sửa, xóa** mục đã ghi | Cao |
| F10 | Dashboard trong ngày | Vòng tròn calo còn lại + 3 thanh macro + danh sách món đã ăn | Cao |
| F11 | Tìm kiếm & lọc món | Tìm theo tên (không dấu/có dấu), lọc theo nhóm (món nước, cơm, bánh mì, trái cây...) | Cao |
| F12 | Lịch sử & thống kê | Xem theo ngày, biểu đồ 7 ngày / 30 ngày, trung bình calo | Trung bình |
| F13 | Thông báo nhắc nhở | Nhắc ghi bữa sáng/trưa/tối theo giờ, bật/tắt trong Cài đặt | Trung bình |
| F14 | Cài đặt | Đơn vị (g/ml), dark mode, xóa toàn bộ dữ liệu, đăng xuất | Trung bình |

### 8.2 Chức năng mở rộng (làm nếu còn thời gian – dùng để "ghi điểm")

| ID | Chức năng | Ghi chú |
|---|---|---|
| E1 | Gợi ý thực đơn theo calo còn lại | Rất dễ làm từ DB local, hiệu quả demo cao |
| E2 | Quét mã vạch sản phẩm đóng gói | ML Kit Barcode → tra DB/API |
| E3 | Chatbot tư vấn dinh dưỡng (Gemini/OpenAI API) | Cần API key, nên làm sau tuần 4 |
| E4 | Đồng bộ Firestore đa thiết bị | Backup nhật ký |
| E5 | Xuất báo cáo PDF / chia sẻ ảnh tổng kết | Dùng PdfDocument |
| E6 | Nhận diện nhiều món trong 1 ảnh | Khó, chỉ làm nếu model tốt |
| E7 | Widget màn hình chính / Wear OS | Ngoài phạm vi môn học, chỉ nêu hướng phát triển |
| E8 | Cộng đồng chia sẻ món ăn, đánh giá | Cần backend + moderation, không làm |
| E9 | Streak + huy hiệu (gamification) | Dễ, tăng trải nghiệm |

### 8.3 Ngoài phạm vi (nói rõ để bảo vệ đồ án)

- Không chẩn đoán bệnh, không thay thế bác sĩ/chuyên gia dinh dưỡng.
- Không ước lượng khẩu phần tự động bằng AI (chỉ chọn thủ công).
- Không xây web admin riêng (dữ liệu món quản lý bằng file CSV/JSON + script).
- Không hỗ trợ iOS trong học kỳ này.

---

## 9. Công nghệ sử dụng

### 9.1 Bảng công nghệ (phương án khuyến nghị – Android native)

| Thành phần | Công nghệ | Lý do chọn |
|---|---|---|
| Ngôn ngữ | **Kotlin** | Chuẩn hiện nay của Android, ít lỗi runtime hơn Java |
| Giao diện | **Jetpack Compose** (+ Material 3) | Dựng UI nhanh, dễ làm dark mode, ít file XML |
| Kiến trúc | **MVVM + Repository** (+ Hilt DI) | Tách UI – logic – dữ liệu, dễ test, đúng yêu cầu tuần 3 |
| Local DB | **Room** (SQLite) | Type-safe, hỗ trợ Flow, dễ seed dữ liệu từ JSON |
| Nhận diện ảnh | **TensorFlow Lite** – MobileNetV2 fine-tune bằng **Teachable Machine** | Chạy offline, miễn phí, tự train được món Việt |
| Camera | **CameraX** | API đơn giản, hỗ trợ nhiều thiết bị |
| Gọi API | **Retrofit + OkHttp + Moshi** (CalorieNinjas / Edamam – gói free) | Đúng yêu cầu "gọi API", chỉ dùng bổ sung |
| Xác thực | **Firebase Authentication** (Email + Google) | Không phải tự viết server, có sẵn quên mật khẩu |
| Cloud (mở rộng) | **Firebase Firestore** | Backup nhật ký |
| Biểu đồ | **MPAndroidChart** hoặc **Vico** (Compose) | Biểu đồ 7/30 ngày |
| Thông báo | **WorkManager + NotificationCompat** | Nhắc nhở offline, không cần server |
| Load ảnh | **Coil** | Nhẹ, hỗ trợ Compose |
| Quản lý mã nguồn | **Git + GitHub** (branch: `main`, `dev`, `feature/*`) | Bắt buộc để chấm điểm quy trình |
| Thiết kế | **Figma** | Wireframe + mockup tuần 2 |

### 9.2 Phương án thay thế (nếu lớp học dùng công nghệ khác)

| Nếu môn học dạy... | Điều chỉnh |
|---|---|
| **Java + XML truyền thống** | Giữ nguyên kiến trúc MVVM; thay Compose bằng ViewBinding + RecyclerView; thay Hilt bằng ViewModelProvider.Factory |
| **Flutter** | Flutter + Provider/Riverpod + sqflite/Drift; TFLite qua `tflite_flutter`; Firebase Auth; camera qua `image_picker` |
| **React Native** | Expo + SQLite + `react-native-fast-tflite` (khó hơn) → **không khuyến nghị** cho đề tài này |
| **Không được dùng Firebase** | Tự lưu tài khoản bằng Room + hash mật khẩu bằng BCrypt (PIN local) |

> ⚠️ **Chốt công nghệ ngay tuần 1**, không đổi sau tuần 2.

---

## 10. Kiến trúc sơ bộ (chi tiết ở tuần 3)

```
┌───────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + ViewModel)       │
│  Auth • Home/Dashboard • Camera • Diary       │
│  Search • Stats • Profile • Settings          │
└──────────────────┬────────────────────────────┘
                   │ StateFlow / UiState
┌──────────────────▼────────────────────────────┐
│  Domain / Repository Layer                    │
│  AuthRepository • FoodRepository              │
│  DiaryRepository • RecognitionRepository      │
└───────┬───────────────────────┬───────────────┘
        │                       │
┌───────▼────────┐    ┌─────────▼─────────────┐
│ Local: Room DB │    │ Remote: Firebase Auth │
│ foods, diary,  │    │ + Nutrition API       │
│ users, portion │    │ (Retrofit, optional)  │
└───────┬────────┘    └───────────────────────┘
        │
┌───────▼─────────────────────────────┐
│ On-device AI: TFLite food model     │
│ (assets/nutrivision_food.tflite)    │
└─────────────────────────────────────┘
```

**Luồng xử lý ảnh (quan trọng – nêu ở tuần 1 để chứng minh đã suy nghĩ kỹ):**

```
Chụp/chọn ảnh → Resize 224×224 → TFLite → Top-3 nhãn + confidence
   → Nếu confidence ≥ 0.6: gợi ý món số 1 (vẫn cho sửa)
   → Nếu < 0.6: hiện top-3 + ô tìm kiếm tay
   → Chọn khẩu phần → Tính dinh dưỡng = (giá trị/100g) × gram
   → Lưu vào nhật ký (Room) → Cập nhật Dashboard
```

---

## 11. Rủi ro & phương án dự phòng

| # | Rủi ro | Xác suất | Ảnh hưởng | Phương án |
|---|---|---|---|---|
| R1 | Model nhận diện kém | Cao | Cao | Train trên ảnh tự chụp cùng điều kiện ánh sáng; luôn có bước xác nhận; demo bằng món đã train |
| R2 | Thiếu dữ liệu dinh dưỡng món Việt | Cao | Cao | Bắt đầu thu thập từ **tuần 1**, mục tiêu 20 món/tuần (3 người × ~7 món) |
| R3 | TV nào đó bận/ốm, trễ tiến độ | Trung bình | Cao | Mỗi module có 1 người chính + 1 người backup; code review chéo; commit hằng ngày |
| R4 | Firebase hết quota / lỗi cấu hình SHA-1 | Trung bình | Trung bình | Có sẵn nhánh "local auth" dự phòng; cấu hình Firebase ngay tuần 3 |
| R5 | Máy ảo không có camera | Cao | Thấp | Luôn test bằng ảnh trong thư viện + dùng `image_picker`; quay video demo bằng máy thật |
| R6 | Trễ tiến độ tuần 4 | Trung bình | Cao | Cắt E* trước, giữ F1–F10; F11–F14 có thể lùi |

---

## 12. Checklist hoàn thành tuần 1

- [x] Tên đề tài + slogan
- [x] Lý do chọn (5 lý do, có số liệu/định tính)
- [x] Mục đích + 5 mục tiêu đo lường được
- [x] 3 persona + actor phụ
- [x] 7 vấn đề người dùng ↔ cách giải quyết
- [x] So sánh 3 ứng dụng tương tự + bảng tổng hợp + kết luận định vị
- [x] 14 chức năng chính + 9 chức năng mở rộng + phạm vi loại trừ
- [x] Sơ đồ Use Case (file `02_UseCase_Diagram.puml`)
- [x] Chọn công nghệ + phương án thay thế
- [x] Phân công 3 người + kế hoạch 6 tuần (file `03_KeHoach_va_PhanCong.md`)
- [ ] **Việc cần làm ngay sau buổi trình bày:** bắt đầu thu thập 20 món × ảnh + số liệu dinh dưỡng
