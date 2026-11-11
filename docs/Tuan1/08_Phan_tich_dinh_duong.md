# PHÂN TÍCH DINH DƯỠNG — Làm thế nào, có cần model không?

> **Trả lời ngắn:** **Không cần model.** AI chỉ làm 1 việc: *nhận diện món*. Dinh dưỡng là **tra bảng + nhân chia + so với mục tiêu** — hoàn toàn xác định (deterministic), không đoán, không ảo giác.

---

## 1. Tách bài toán thành 3 tầng

```
┌──────────────────────────────────────────────────────────────┐
│ TẦNG 1 — NHẬN DIỆN:  "Đây là món gì?"                        │
│ Công cụ: model TFLite (MobileNet)                            │
│ Đầu ra: phở bò (87%)                                        │
└───────────────────────────┬──────────────────────────────────┘
                            ▼
┌──────────────────────────────────────────────────────────────┐
│ TẦNG 2 — ĐỊNH LƯỢNG:  "Bao nhiêu gram?"                      │
│ Công cụ: NGƯỜI DÙNG chọn khẩu phần (1 tô vừa = 500g)         │
│ (đây là chỗ các app khác dùng AI và sai 20–30%)              │
└───────────────────────────┬──────────────────────────────────┘
                            ▼
┌──────────────────────────────────────────────────────────────┐
│ TẦNG 3 — TRA CỨU + TÍNH + ĐÁNH GIÁ                           │
│ Công cụ: bảng dinh dưỡng (Room) + công thức + luật cảnh báo  │
│ Đầu ra: 460 kcal, 32g đạm, 57g carb, 11g béo, 2250mg natri   │
└──────────────────────────────────────────────────────────────┘
```

Chỉ **tầng 1** dùng model. Tầng 2 dùng con người. Tầng 3 dùng dữ liệu + code.

---

## 2. Vì sao không có "model dinh dưỡng"?

Vì **calo không nằm trong pixel**. Trong cùng một bức ảnh tô phở:
- Bao nhiêu gram bánh? (không thấy được)
- Nước dùng nêm bao nhiêu đường, bao nhiêu muối? (vô hình)
- Thịt béo hay nạc? (khó phân biệt)
- Có thêm 1 thìa dầu ớt không? (ẩn dưới mặt nước)

**Bằng chứng từ nghiên cứu** (Nutrition5k – Google, CVPR 2021, bộ dữ liệu 5.000 đĩa ăn có cân thật):

| Phương pháp | Sai số calo |
|---|---|
| Luôn đoán bằng giá trị trung bình | 60% |
| Model 2D nhìn 1 ảnh, dự đoán trực tiếp calo | **26%** |
| Model có **camera depth** (đo thể tích) | 18,8% |
| Model có depth + thể tích | 16,5% |
| Model biết **khối lượng thật** → chỉ tra calo/g | **9,5%** |

→ Kết luận rút ra: **cho model biết khối lượng (do người dùng chọn) chính là cách giảm sai số mạnh nhất** — từ 26% xuống 9,5%. Đây chính là lý do nhóm chọn "chọn khẩu phần thủ công" thay vì "AI ước lượng khẩu phần".

Và câu này nên đưa nguyên văn vào báo cáo:
> "Theo Nutrition5k (Google, CVPR 2021), dự đoán calo trực tiếp từ một ảnh có sai số trung bình 26%; khi biết khối lượng, sai số giảm còn 9,5%. Do đó nhóm tách bài toán: AI nhận diện món, người dùng xác nhận khẩu phần, hệ thống tra bảng dinh dưỡng — vừa chính xác hơn vừa giải thích được."

---

## 3. Tầng 2 — Định lượng: chọn khẩu phần thế nào cho "đủ tốt"

### 3.1 Bảng khẩu phần mặc định (đưa vào DB)

| Món | Nhỏ | Vừa (mặc định) | Lớn |
|---|---|---|---|
| Phở / bún / mì | 350 g (tô nhỏ) | **500 g** | 700 g (tô lớn) |
| Cơm tấm / cơm đĩa | 300 g | **400 g** | 500 g |
| Bánh mì | 150 g (nửa ổ) | **250 g** (1 ổ) | 350 g |
| Gỏi cuốn | 50 g/cuốn (×1) | **60 g** (×2) | 80 g (×3) |
| Chả giò | 40 g/cuốn | **40 g** | 60 g |
| Trứng luộc | 50 g/quả | **50 g** | 60 g |
| Đĩa rau xào | 100 g | **150 g** | 200 g |
| Đĩa trái cây | 100 g | **150 g** | 250 g |
| Ly đồ uống | 200 ml | **300 ml** | 500 ml |

Người dùng chỉ cần bấm 1 trong 3 nút. **3 giây**, không cần AI.

### 3.2 Hệ số chế biến (chỉ dùng khi DB là số liệu NGUYÊN LIỆU SỐNG)

| Cách chế biến | Hệ số |
|---|---|
| Luộc / hấp | 1,00 |
| Nướng | 1,10 |
| Xào | 1,15 |
| Chiên / rán | 1,30 |
| Chiên ngập dầu | 1,40 |

> ⚠️ **Bẫy double-count:** DB của nhóm là số liệu **món đã nấu** (phở bò 92 kcal/100g đã tính cả nước dùng và dầu) → **không nhân hệ số**. Chỉ nhân khi DB ghi "thịt heo sống" rồi người dùng chọn "chiên". Ghi rõ cột `is_cooked` trong bảng `foods` (xem file 04).

---

## 4. Tầng 3 — Công thức & code

### 4.1 Công thức cốt lõi

```
giá_trị = giá_trị_trên_100g × gram / 100        (× hệ số chế biến nếu cần)

kcal_ước_tính_lại = protein×4 + carb×4 + fat×9   ← dùng để KIỂM TRA dữ liệu
```

Ví dụ thật: **Phở bò, 1 tô vừa 500 g** (92 kcal/100g)

| Chỉ số | Tính | Kết quả |
|---|---|---|
| Calo | 92 × 5 | **460 kcal** |
| Đạm | 6,5 × 5 | 32,5 g |
| Carb | 11,5 × 5 | 57,5 g |
| Béo | 2,2 × 5 | 11,0 g |
| Natri | 450 × 5 | **2.250 mg** ⚠️ vượt ngưỡng 2.000 mg/ngày |

Kiểm tra chéo: 32,5×4 + 57,5×4 + 11×9 = 130 + 230 + 99 = **459 kcal** ≈ 460 ✅ (lệch <1% → dữ liệu đúng)

> Mẹo QA cho TV2: chạy script kiểm tra `|kcal - (P*4+C*4+F*9)| / kcal > 0.15` → in ra danh sách món có dữ liệu sai. Chỉ mất 20 phút, cứu được rất nhiều lỗi nhập liệu.

### 4.2 Code Kotlin

```kotlin
data class Food(
    val id: Long,
    val name: String,
    val category: String,          // "Món nước", "Cơm", "Rau", "Trái cây", "Đồ uống"
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double,
    val sodiumPer100g: Double,
    val sugarPer100g: Double = 0.0,
    val defaultPortionG: Int,
    val portionLabel: String
)

data class Macro(
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val carb: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sodium: Double = 0.0,
    val sugar: Double = 0.0
) {
    operator fun plus(o: Macro) = Macro(
        kcal + o.kcal, protein + o.protein, carb + o.carb,
        fat + o.fat, fiber + o.fiber, sodium + o.sodium, sugar + o.sugar
    )
}

object NutritionCalculator {

    /** cookingFactor: 1.0 nếu DB đã là món chín */
    fun compute(food: Food, grams: Double, cookingFactor: Double = 1.0): Macro {
        val k = grams / 100.0 * cookingFactor
        return Macro(
            kcal    = food.kcalPer100g    * k,
            protein = food.proteinPer100g * k,
            carb    = food.carbPer100g    * k,
            fat     = food.fatPer100g     * k,
            fiber   = food.fiberPer100g   * k,
            sodium  = food.sodiumPer100g  * k,
            sugar   = food.sugarPer100g   * k
        )
    }

    fun sum(items: List<Pair<Food, Double>>): Macro =
        items.fold(Macro()) { acc, (food, grams) -> acc + compute(food, grams) }

    /** % năng lượng từ từng chất — dùng cho biểu đồ tròn */
    fun energyRatio(m: Macro): Triple<Int, Int, Int> {
        val p = m.protein * 4; val c = m.carb * 4; val f = m.fat * 9
        val total = (p + c + f).takeIf { it > 0 } ?: return Triple(0, 0, 0)
        return Triple(
            (p / total * 100).toInt(),
            (c / total * 100).toInt(),
            (f / total * 100).toInt()
        )
    }

    /** Kiểm tra chéo dữ liệu trong DB */
    fun isDataConsistent(food: Food): Boolean {
        val fromMacro = food.proteinPer100g * 4 + food.carbPer100g * 4 + food.fatPer100g * 9
        if (food.kcalPer100g <= 0) return false
        return kotlin.math.abs(fromMacro - food.kcalPer100g) / food.kcalPer100g <= 0.15
    }
}
```

### 4.3 Mục tiêu cá nhân (dùng cho Dashboard)

```kotlin
object TargetCalculator {

    fun bmr(weightKg: Double, heightCm: Double, age: Int, isMale: Boolean): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (isMale) base + 5 else base - 161
    }

    fun tdee(bmr: Double, activity: ActivityLevel): Double = bmr * activity.factor

    fun dailyTarget(tdee: Double, goal: Goal): Macro {
        val kcal = when (goal) {
            Goal.LOSE     -> tdee - 500
            Goal.MAINTAIN -> tdee
            Goal.GAIN     -> tdee + 300
        }
        // Tỉ lệ vàng: đạm 25% – carb 45% – béo 30%
        return Macro(
            kcal    = kcal,
            protein = kcal * 0.25 / 4,
            carb    = kcal * 0.45 / 4,
            fat     = kcal * 0.30 / 9,
            fiber   = 25.0,          // WHO: ≥ 25 g/ngày
            sodium  = 2000.0,        // WHO: < 2.000 mg/ngày
            sugar   = 25.0           // WHO: < 25 g đường tự do/ngày
        )
    }
}

enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2), LIGHT(1.375), MODERATE(1.55), ACTIVE(1.725)
}
enum class Goal { LOSE, MAINTAIN, GAIN }
```

**Ví dụ:** nam, 20 tuổi, 65 kg, 170 cm, vận động nhẹ, mục tiêu giảm cân
- BMR = 10×65 + 6.25×170 − 5×20 + 5 = **1.617 kcal**
- TDEE = 1.617 × 1.375 ≈ **2.224 kcal**
- Mục tiêu = 2.224 − 500 ≈ **1.724 kcal/ngày** → đạm 108 g, carb 194 g, béo 57 g

---

## 5. "Phân tích" thực chất là gì? (phần tạo ra giá trị)

Tính ra con số chưa phải là "phân tích". Phân tích = **so sánh + cảnh báo + gợi ý**:

```kotlin
enum class Level { OK, WARN, BAD }

data class Insight(val level: Level, val title: String, val message: String)

object NutritionAnalyzer {

    fun analyze(today: Macro, target: Macro, meals: List<Food>): List<Insight> {
        val out = mutableListOf<Insight>()

        // 1. Năng lượng
        val kcalPct = today.kcal / target.kcal
        when {
            kcalPct > 1.10 -> out += Insight(
                Level.BAD, "Vượt ${((kcalPct - 1) * 100).toInt()}% calo",
                "Hôm nay bạn đã nạp ${today.kcal.toInt()} kcal, vượt mục tiêu ${target.kcal.toInt()} kcal."
            )
            kcalPct < 0.70 && java.time.LocalTime.now().hour >= 20 -> out += Insight(
                Level.WARN, "Ăn quá ít",
                "Bạn mới nạp ${(kcalPct * 100).toInt()}% mục tiêu. Ăn quá ít kéo dài dễ mất cơ."
            )
            else -> out += Insight(Level.OK, "Năng lượng trong tầm kiểm soát", "Còn ${(target.kcal - today.kcal).toInt()} kcal.")
        }

        // 2. Đạm
        if (today.protein < target.protein * 0.8)
            out += Insight(Level.WARN, "Thiếu đạm",
                "Còn thiếu ${(target.protein - today.protein).toInt()} g. Thêm trứng, ức gà, đậu hũ hoặc cá.")

        // 3. Natri (WHO < 2000 mg)
        if (today.sodium > 2000)
            out += Insight(Level.BAD, "Natri cao",
                "${today.sodium.toInt()} mg — vượt ngưỡng WHO. Hạn chế nước mắm, mì gói, đồ muối.")

        // 4. Đường (WHO < 25 g)
        if (today.sugar > 25)
            out += Insight(Level.BAD, "Đường vượt ngưỡng",
                "${today.sugar.toInt()} g đường tự do. Cắt bớt trà sữa/nước ngọt.")

        // 5. Chất xơ
        if (today.fiber < 20)
            out += Insight(Level.WARN, "Thiếu chất xơ",
                "Mới ${today.fiber.toInt()}/25 g. Thêm rau xanh hoặc trái cây.")

        // 6. Cân đối macro
        val (p, c, f) = NutritionCalculator.energyRatio(today)
        if (f > 40) out += Insight(Level.WARN, "Nhiều chất béo",
            "Chất béo chiếm $f% năng lượng (khuyến nghị 25–30%).")

        // 7. Thiếu rau
        if (meals.none { it.category == "Rau" || it.category == "Trái cây" })
            out += Insight(Level.WARN, "Chưa có rau/trái cây", "Thêm 1 đĩa rau hoặc 1 quả cho bữa này.")

        return out
    }
}
```

### 5.1 Điểm dinh dưỡng 0–100 (tuỳ chọn, tăng trải nghiệm)

```kotlin
fun nutritionScore(today: Macro, target: Macro): Int {
    var score = 100
    if (today.kcal > target.kcal * 1.10 || today.kcal < target.kcal * 0.70) score -= 15
    if (today.protein < target.protein * 0.80) score -= 10
    if (today.fiber   < 20)                    score -= 10
    if (today.sodium  > 2000)                  score -= 15
    if (today.sugar   > 25)                    score -= 10
    val (_, _, f) = NutritionCalculator.energyRatio(today)
    if (f > 40)                                score -= 10
    return score.coerceIn(0, 100)
}
```

---

## 6. Luồng UI hoàn chỉnh (từ ảnh đến "phân tích")

```
[1] Chụp ảnh
      ↓  ~100ms
[2] AI: top-3 món + %
      ↓  người dùng bấm 1 nút (hoặc sửa)
[3] Chọn khẩu phần:  Nhỏ | Vừa | Lớn  (mặc định = Vừa)
      ↓
[4] Màn kết quả:
      ┌──────────────────────────────┐
      │  Phở bò • 1 tô vừa (500 g)   │
      │  460 kcal                    │
      │  ▓▓▓▓▓░░░░  Đạm   32 g       │
      │  ▓▓▓▓▓▓▓▓░  Carb  57 g       │
      │  ▓▓░░░░░░░  Béo   11 g       │
      │  ⚠ Natri 2.250 mg (112% WHO) │
      └──────────────────────────────┘
      ↓  Lưu
[5] Dashboard cập nhật + hiện Insight list
      ↓
[6] 20:00 → Notification tổng kết ngày + điểm dinh dưỡng
```

---

## 7. Dữ liệu dinh dưỡng lấy ở đâu (nhắc lại, quan trọng)

| Nguồn | Ưu tiên | Ghi chú |
|---|---|---|
| **Bảng thành phần dinh dưỡng thực phẩm Việt Nam** (Viện Dinh dưỡng QG, 2017) | ⭐⭐⭐ | Chuẩn nhất cho món Việt |
| USDA FoodData Central | ⭐⭐ | Nguyên liệu, món quốc tế |
| Nhãn dinh dưỡng bao bì | ⭐⭐ | Đồ uống, sữa, bánh — chụp ảnh làm minh chứng |
| CalorieNinjas / Edamam | ⭐ | Chỉ để đối chiếu, không làm nguồn chính |

**Cách nhóm nên làm:** mỗi món ghi 1 dòng trong `foods.csv` + 1 ảnh chụp nguồn (trang sách/nhãn) lưu vào thư mục `evidence/`. Khi hội đồng hỏi "số liệu này ở đâu ra?" → mở ra ngay. Đây là chi tiết nhỏ nhưng tạo niềm tin rất lớn.

---

## 8. Nếu vẫn muốn "AI làm dinh dưỡng" thì sao?

| Cách | Thực tế | Khuyến nghị |
|---|---|---|
| Dùng Gemini/GPT-4o ước lượng calo từ ảnh | Trả lời trôi chảy nhưng **hay bịa số**, sai 20–40%, không tái lập được | ❌ Không dùng làm số liệu chính |
| Dùng AI để **gợi ý món tiếp theo** dựa trên calo còn lại | Rủi ro thấp vì chỉ là gợi ý | ✅ Được, làm ở tuần 5 |
| Train model hồi quy calo (kiểu Nutrition5k) | Cần dataset có cân thật, cần depth để bớt sai số | ❌ Quá sức trong 6 tuần |
| **Tra bảng + người dùng chọn khẩu phần** (cách của nhóm) | Sai số ~10–15% (chủ yếu do khẩu phần), giải thích được, offline | ✅ **Chọn cái này** |

**Câu chốt khi bảo vệ:**
> "Ứng dụng không dùng AI để đoán calo, vì calo không nhìn thấy được từ ảnh. AI xác định *món gì*, người dùng xác nhận *bao nhiêu*, hệ thống tra bảng dinh dưỡng đã kiểm chứng để tính. Nhờ vậy kết quả minh bạch, tái lập được và sai số thấp hơn cách dự đoán trực tiếp từ ảnh."

---

## 9. Checklist cho TV2 (tuần 3–4)

- [ ] `foods.csv` đủ 100+ món, có `is_cooked`
- [ ] Chạy `isDataConsistent()` trên toàn bộ → sửa các món lệch >15%
- [ ] Bảng `portion_presets` (nhỏ/vừa/lớn) cho từng món
- [ ] `NutritionCalculator` + unit test 5 case (0g, 100g, 500g, hệ số chiên, tổng nhiều món)
- [ ] `TargetCalculator` test với 3 profile mẫu
- [ ] `NutritionAnalyzer` trả về ≥ 1 insight cho mọi trường hợp (kể cả ngày hoàn hảo)
