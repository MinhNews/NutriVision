# Nguồn dữ liệu dinh dưỡng — 145 mục

File dữ liệu: `ai_tools/_source/foods_nutrition.json` (bản app dùng: `app/src/main/assets/foods_nutrition.json`, giống từng byte).

Sinh lại bằng: `python ai_tools/_source/nutrition_build.py` (thêm `--dry-run` để xem trước).

## Nguyên tắc

1. Mỗi mục là **một khẩu phần ghi rõ bằng gram** (`portion_name`, `portion_weight_g`), không phải "một tô" chung chung.
2. Mỗi mục có `method` cho biết số liệu từ đâu:
   - `direct` — lấy nguyên từ USDA FoodData Central, có `fdcId` trong `source`.
   - `recipe` — cộng nguyên liệu theo gram (bảng thành phần VN FCT 2017 / USDA) rồi chia suất. Ước lượng, sai số thực tế khoảng ±15–20 %.
   - `estimate` — số nhập tay từ giai đoạn đầu, calo khớp với đạm/carb/béo nhưng **chưa đối chiếu nguồn ngoài**.
3. `confidence` chỉ nói về độ tin của **nguồn**, không phải độ ngon của món.
4. Có sẵn `*_per_100g` để app tính lại khi người dùng kéo khẩu phần, không phải chia tại chỗ.
5. Không có mục nào bị bịa số. Mục nào không có nguồn thì ghi `estimate` + `low`.

## Tóm tắt

| Nguồn | Số mục | Ghi chú |
|---|---|---|
| `direct` (USDA) | 4 | tra được mã fdcId |
| `recipe` (công thức) | 40 | ước lượng ±15–20 % |
| `estimate` (nhập tay cũ) | 101 | chưa có bản ghi nguồn |
| **Tổng** | **145** | 131 lớp model + 14 món tra cứu tay |

Kiểm tra tự động khi build: calo phải khớp `4×đạm + 4×carb + 9×béo` trong 10 % (15 % với mục `estimate`), xơ ≤ carb, khẩu phần 5–900 g, 5–600 kcal/100 g, và `*_per_100g` phải khớp lại đúng với số của khẩu phần.

## 4 mục lấy trực tiếp từ USDA

| id | Khẩu phần | Dinh dưỡng / khẩu phần | Nguồn |
|---|---|---|---|
| `cheesecake` | 1 miếng (125g) | 499 kcal · P 6.6 · C 43.5 · F 33.1 · xơ 0.6 · Na 346 mg | USDA FNDDS 2707861 (Cheesecake, plain) |
| `chicken_wings` | 4 cánh (140g thịt) | 364 kcal · P 30.0 · C 5.6 · F 23.7 · xơ 0.3 · Na 584 mg | USDA FNDDS 2706069 (Chicken wing, baked, coated) |
| `grilled_salmon` | 1 miếng phi lê (150g) | 411 kcal · P 38.1 · C 0.0 · F 27.6 · xơ 0.0 · Na 90 mg | USDA FNDDS 2706286 (Salmon, baked or broiled) |
| `macaroni_and_cheese` | 1 bát (250g) | 558 kcal · P 21.8 · C 57.8 · F 26.3 · xơ 3.0 · Na 925 mg | USDA FNDDS 2708811 (Macaroni with cheese) |

## 40 mục tính từ công thức

Nguyên liệu cho **cả nồi / cả mẻ**, chia theo số suất; `portion_weight_g` = tổng gram ÷ số suất, nên tên khẩu phần và con số luôn khớp nhau.

| id | Khẩu phần | Dinh dưỡng / khẩu phần | kcal/100 g |
|---|---|---|---|
| `baba_nau_chuoi_dau` | 1 bát (480g) | 417 kcal · P 30.9 · C 46.3 · F 13.0 · xơ 4.4 · Na 562 mg | 86.9 |
| `banh_bo` | 1 miếng (86g) | 207 kcal · P 1.8 · C 36.7 · F 6.2 · xơ 1.1 · Na 5 mg | 240.7 |
| `banh_da_lon` | 1 miếng (70g) | 134 kcal · P 1.4 · C 22.4 · F 4.8 · xơ 1.5 · Na 3 mg | 191.4 |
| `banh_duc` | 1 bát (312g) | 268 kcal · P 8.1 · C 30.2 · F 11.7 · xơ 0.9 · Na 1198 mg | 85.9 |
| `banh_gai` | 1 cái (120g) | 285 kcal · P 5.2 · C 47.5 · F 8.7 · xơ 4.2 · Na 5 mg | 237.5 |
| `banh_giay` | 1 cái (100g) | 196 kcal · P 4.6 · C 39.7 · F 2.0 · xơ 2.3 · Na 3 mg | 196.0 |
| `banh_hoi` | 1 phần (128g) | 205 kcal · P 3.0 · C 39.6 · F 3.0 · xơ 1.2 · Na 293 mg | 160.2 |
| `banh_la` | 1 cái (145g) | 441 kcal · P 10.1 · C 60.8 · F 17.1 · xơ 3.9 · Na 275 mg | 304.1 |
| `banh_tai_heo` | 1 cái (31g) | 125 kcal · P 1.3 · C 13.7 · F 7.3 · xơ 0.4 · Na 1 mg | 403.2 |
| `banh_tom_ho_tay` | 1 cái (70g) | 159 kcal · P 4.4 · C 12.7 · F 10.2 · xơ 1.0 · Na 106 mg | 227.1 |
| `banh_trung_thu` | 1 cái (150g) | 582 kcal · P 8.5 · C 86.3 · F 21.5 · xơ 2.8 · Na 132 mg | 388.0 |
| `banh_u` | 1 cái (133g) | 424 kcal · P 9.4 · C 58.9 · F 16.2 · xơ 3.2 · Na 209 mg | 318.8 |
| `ca_muoi_xoi` | 1 phần (146g) | 243 kcal · P 23.3 · C 3.3 · F 15.0 · xơ 0.1 · Na 1529 mg | 166.4 |
| `ca_ri_ga` | 1 tô (541g) | 628 kcal · P 42.7 · C 26.3 · F 40.1 · xơ 6.4 · Na 586 mg | 116.1 |
| `ca_sot_ca_chua` | 1 phần (309g) | 279 kcal · P 29.5 · C 10.8 · F 13.2 · xơ 1.6 · Na 588 mg | 90.3 |
| `canh_bi_do` | 1 bát (361g) | 126 kcal · P 5.8 · C 9.1 · F 7.9 · xơ 0.8 · Na 311 mg | 34.9 |
| `canh_kho_hoa` | 1 bát (424g) | 199 kcal · P 12.5 · C 7.4 · F 13.4 · xơ 5.4 · Na 340 mg | 46.9 |
| `canh_khoai_tim` | 1 bát (434g) | 353 kcal · P 14.9 · C 34.1 · F 17.5 · xơ 5.3 · Na 354 mg | 81.3 |
| `cha_com` | 1 cái (48g) | 146 kcal · P 6.6 · C 5.3 · F 10.6 · xơ 0.2 · Na 90 mg | 304.2 |
| `cha_lui` | 2 cây (142g) | 436 kcal · P 18.0 · C 8.6 · F 35.9 · xơ 0.3 · Na 536 mg | 307.0 |
| `dumplings` | 6 viên (182g) | 375 kcal · P 16.2 · C 40.9 · F 15.7 · xơ 2.3 · Na 263 mg | 206.0 |
| `fish_and_chips` | 1 phần (452g) | 1113 kcal · P 37.3 · C 110.1 · F 56.4 · xơ 8.5 · Na 1278 mg | 246.2 |
| `goi_ca_chich` | 1 phần (350g) | 404 kcal · P 29.0 · C 40.6 · F 15.3 · xơ 3.8 · Na 1261 mg | 115.4 |
| `khau_nhuc` | 1 phần (202g) | 801 kcal · P 13.8 · C 18.9 · F 74.2 · xơ 2.4 · Na 442 mg | 396.5 |
| `kho_muc_nuong` | 1 con (100g) | 313 kcal · P 60.3 · C 7.5 · F 4.0 · xơ 0.0 · Na 1700 mg | 313.0 |
| `luon_om_chuoi_dau` | 1 bát (509g) | 614 kcal · P 37.8 · C 45.4 · F 31.8 · xơ 4.4 · Na 700 mg | 120.6 |
| `luon_xao_xa_ot` | 1 phần (199g) | 438 kcal · P 28.8 · C 10.3 · F 31.0 · xơ 0.4 · Na 624 mg | 220.1 |
| `mam_chung` | 1 bát (148g) | 265 kcal · P 17.8 · C 10.0 · F 16.4 · xơ 0.2 · Na 3066 mg | 179.1 |
| `mam_tep_chung_thit` | 1 bát (140g) | 318 kcal · P 18.8 · C 11.2 · F 21.1 · xơ 0.2 · Na 3306 mg | 227.1 |
| `mi_xao_gion` | 1 đĩa (405g) | 932 kcal · P 26.8 · C 63.0 · F 64.4 · xơ 4.6 · Na 1207 mg | 230.1 |
| `nachos` | 1 phần (240g) | 788 kcal · P 23.4 · C 67.5 · F 47.1 · xơ 4.8 · Na 850 mg | 328.3 |
| `nui_xao` | 1 đĩa (298g) | 459 kcal · P 24.9 · C 53.4 · F 16.0 · xơ 4.2 · Na 523 mg | 154.0 |
| `oc_len_xao_dua` | 1 phần (208g) | 365 kcal · P 18.7 · C 14.3 · F 26.8 · xơ 1.9 · Na 652 mg | 175.5 |
| `onion_rings` | 1 phần (217g) | 602 kcal · P 6.2 · C 41.3 · F 46.5 · xơ 2.8 · Na 793 mg | 277.4 |
| `takoyaki` | 6 viên (270g) | 537 kcal · P 16.7 · C 37.6 · F 35.6 · xơ 2.1 · Na 825 mg | 198.9 |
| `tau_hu_nhoi_thit` | 1 phần (266g) | 418 kcal · P 28.6 · C 7.3 · F 31.2 · xơ 2.9 · Na 346 mg | 157.1 |
| `tau_hu_non` | 1 chén (203g) | 112 kcal · P 10.7 · C 4.5 · F 6.0 · xơ 0.7 · Na 24 mg | 55.2 |
| `tiet_canh` | 1 bát (222g) | 278 kcal · P 19.5 · C 4.0 · F 20.8 · xơ 1.0 · Na 520 mg | 125.2 |
| `tiramisu` | 1 miếng (153g) | 510 kcal · P 7.6 · C 40.4 · F 36.7 · xơ 1.3 · Na 77 mg | 333.3 |
| `trung_vit_lon` | 1 quả (85g) | 155 kcal · P 11.6 · C 1.2 · F 11.6 · xơ 0.0 · Na 102 mg | 182.4 |

## Giới hạn phải nói rõ trong báo cáo

- Bảng thành phần dinh dưỡng Việt Nam cho **nguyên liệu / 100 g**, không có món hoàn chỉnh. Vì vậy món Việt phải tính từ công thức — đó là ước lượng có cơ sở, không phải số đo.
- Cùng một món, các nguồn công khai lệch nhau tới 2 lần vì khẩu phần khác nhau. Số trong file chỉ đúng với **khẩu phần ghi kèm**.
- 101 mục `estimate` là số nhập tay từ trước, nhất quán nội bộ nhưng chưa có nguồn. Nếu cần trích dẫn trong báo cáo thì phải làm lại theo `direct`/`recipe`.
- Nhân khẩu phần theo tỉ lệ là xấp xỉ: thêm nước dùng vào tô phở gần như không thêm calo, nhưng công thức nhân tuyến tính sẽ tính thêm. Chấp nhận được ở mức đồ án, cần ghi rõ.

