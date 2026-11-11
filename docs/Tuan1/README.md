# TUẦN 1 — NutriVision

Thư mục này chứa **bộ hồ sơ hoàn chỉnh cho tuần 1**. Nhóm chỉ cần điền tên/MSSV, đọc lại, rồi nộp và trình bày.

---

## Nộp gì?

| File | Dùng để | Ghi chú |
|---|---|---|
| **`00_BAO_CAO_TUAN1.md`** | **Báo cáo chính** — nộp cho giảng viên | Copy sang Word/Google Docs là xong. Có sẵn bảng đối chiếu yêu cầu ↔ mục |
| **`NutriVision_Tuan1_DUT.pptx`** | **Slide trình bày** — 15 trang, theo template DUT | Mở bằng PowerPoint, bấm F5 để trình chiếu |
| `NutriVision_Tuan1_DUT.pdf` | Bản PDF dự phòng của slide | Xuất lại sau khi điền tên/MSSV |
| `hinh/NutriVision_UseCase_Overview.png` | Hình 1 — Use Case tổng quát | Đã chèn sẵn trong báo cáo |
| `hinh/NutriVision_UseCase_AnalyzeMeal_Detail.png` | Hình 2 — Use Case chi tiết | Đã chèn sẵn trong báo cáo |
| `SLIDE_TUAN1.html` | Bản slide cũ (dự phòng) | Chỉ dùng khi không mở được PowerPoint |

## Tài liệu tham khảo nội bộ (không cần nộp)

| File | Nội dung |
|---|---|
| `01_Tuan1_Mo-ta-de-tai_NutriVision.md` | Bản đầy đủ của phần mô tả đề tài (dài hơn báo cáo) |
| `02_UseCase_Diagram.puml` | Mã nguồn 2 sơ đồ Use Case (sửa rồi render lại) |
| `03_KeHoach_va_PhanCong.md` | Kế hoạch 6 tuần, đặc tả use case, dàn ý slide |
| `04_Dataset_va_Du_lieu.md` | 15 nhãn AI, cách thu ảnh, công thức TDEE, schema bảng `foods` |
| `05_foods_seed_template.csv` | 28 món Việt mẫu để nạp vào Room |
| `06_AI_Train_hay_API.md` | So sánh train vs API, code `TfliteFoodRecognizer`, câu hỏi hội đồng |
| `07_Nguon_Dataset_Mon_Viet.md` | Nguồn dataset món Việt, cách tải, làm sạch dữ liệu |
| `08_Phan_tich_dinh_duong.md` | Công thức, code tính dinh dưỡng, luật cảnh báo, điểm dinh dưỡng |

---

## Trình bày thế nào?

Mở `NutriVision_Tuan1_DUT.pptx` bằng PowerPoint → **F5** để trình chiếu. Nên copy sẵn một bản PDF và một bản trên USB để phòng máy chiếu lỗi.

**Phân chia thời gian (10 phút):**

| Slide | Nội dung | Người nói | Thời lượng |
|---|---|---|---|
| 1–2 | Bìa, nội dung trình bày | TV1 | 30 giây |
| 3–4 | Lý do chọn đề tài, vấn đề và giả thuyết | TV1 | 1,5 phút |
| 5 | Người dùng mục tiêu | TV3 | 45 giây |
| 6–7 | Ba ứng dụng tương tự, bảng so sánh | TV3 | 1,5 phút |
| 8–10 | Chức năng, use case, luồng xử lý | TV2 | 2,5 phút |
| 11–12 | Công nghệ, dữ liệu và bằng chứng | TV1 | 1,5 phút |
| 13–14 | Kế hoạch 6 tuần, phân công và rủi ro | TV2 | 1 phút |
| 15 | Cảm ơn và Q&A | Cả nhóm | 30 giây |

**Xuất PDF dự phòng:** File → Export → Create PDF/XPS → chọn vị trí → Publish.

---

## Việc phải làm trước khi nộp

- [ ] Điền họ tên + MSSV 3 thành viên (mục đầu báo cáo và slide 1)
- [ ] Điền tên lớp, ngày báo cáo (slide 1 đang để trống `Lớp ................`)
- [ ] Chụp màn hình 3 ứng dụng (MyFitnessPal, Foodvisor, Calorie Mama) và chèn vào **phụ lục báo cáo** — slide 6 và 7 có nhắc tới phần này
- [ ] Đọc toàn bộ báo cáo một lượt, sửa lại câu chữ theo cách nói của nhóm
- [ ] Mỗi bạn tập nói phần của mình **2 lần**, canh thời gian
- [ ] Chuẩn bị trả lời 3 câu hỏi ở slide 15
- [ ] Xuất lại PDF sau khi điền tên + copy cả PPTX và PDF sang USB

> Lưu ý: các số liệu trong slide đều có nguồn ở phần chân trang. Những chỗ nhóm chưa kiểm chứng (thời gian tiết kiệm, độ chính xác model, tỉ lệ người dùng) đã ghi rõ là **giả thuyết** hoặc **mục tiêu**, không phải kết quả.

---

## Sau buổi báo cáo (chuẩn bị tuần 2)

1. Chốt danh sách **15 món** sẽ huấn luyện.
2. Mỗi người thu **7 món** (ảnh + số liệu dinh dưỡng/100 g) — dùng `05_foods_seed_template.csv`.
3. Tải dataset: xem `07_Nguon_Dataset_Mon_Viet.md` (30VNFoods + Hugging Face).
4. Tạo repo GitHub, cấu trúc thư mục theo `03_KeHoach_va_PhanCong.md`.
5. TV3 bắt đầu wireframe 8 màn hình chính trên Figma.

---

## Sửa sơ đồ Use Case

```bash
# Cần Java. Tải plantuml.jar một lần:
# https://github.com/plantuml/plantuml/releases
java -jar plantuml.jar -tpng -charset UTF-8 02_UseCase_Diagram.puml -o hinh
```

Hoặc dán nội dung file `.puml` vào https://www.plantuml.com/plantuml/uml/ rồi tải PNG về.
