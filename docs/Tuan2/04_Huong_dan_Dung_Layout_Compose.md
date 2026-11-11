# HƯỚNG DẪN DỰNG LAYOUT TĨNH TRONG PROJECT ANDROID — NUTRIVISION

> **Dành cho TV1 (Android Lead) và TV2:** Tài liệu này hướng dẫn thiết lập thư mục, theme Material 3 và code khung Jetpack Compose để hoàn thành yêu cầu *"Dựng layout tĩnh cho các màn hình chính trong project"* ở Tuần 2.

---

## 1. Cấu trúc thư mục UI đề xuất (Chuẩn Clean Architecture)

```text
app/src/main/java/com/nutrivision/app/
├── ui/
│   ├── theme/
│   │   ├── Color.kt          <-- Định nghĩa bảng màu (Emerald, Amber, Macro colors)
│   │   ├── Type.kt           <-- Định nghĩa kiểu chữ Typography
│   │   └── Theme.kt          <-- Thiết lập Material 3 Theme (Light & Dark mode)
│   ├── components/           <-- Các component dùng chung
│   │   ├── CalorieRing.kt    <-- Vòng tròn tiến độ calo
│   │   ├── MacroProgressBar.kt <-- Thanh tiến độ Đạm/Carb/Béo
│   │   ├── MealCard.kt       <-- Thẻ bữa ăn (Sáng/Trưa/Tối)
│   │   └── NutriButton.kt    <-- Nút bấm chuẩn của app
│   ├── screens/              <-- Các màn hình tĩnh
│   │   ├── dashboard/
│   │   │   └── DashboardScreen.kt
│   │   ├── customizer/
│   │   │   └── MealCustomizerScreen.kt   (Màn hình AI & Tinh chỉnh)
│   │   ├── camera/
│   │   │   └── CameraScanScreen.kt
│   │   ├── analytics/
│   │   │   └── AnalyticsScreen.kt
│   │   └── auth/
│   │       └── LoginScreen.kt
│   └── navigation/
│       └── AppNavGraph.kt    <-- Cấu hình chuyển trang tĩnh
```

---

## 2. Mã nguồn cấu hình Theme Material 3

### 2.1. File `ui/theme/Color.kt`
```kotlin
package com.nutrivision.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors
val PrimaryGreen = Color(0xFF10B981)
val PrimaryGreenDark = Color(0xFF059669)
val SecondaryAmber = Color(0xFFF59E0B)
val AccentIndigo = Color(0xFF6366F1)

// Macro Colors
val MacroProtein = Color(0xFF3B82F6) // Xanh dương
val MacroCarb = Color(0xFFF59E0B)    // Vàng cam
val MacroFat = Color(0xFFEF4444)     // Đỏ

// Neutral Colors
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val BorderLight = Color(0xFFE2E8F0)
```

### 2.2. File `ui/theme/Theme.kt`
```kotlin
package com.nutrivision.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryAmber,
    tertiary = AccentIndigo,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NutriVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 3. Khung code Jetpack Compose cho các màn hình tĩnh cốt lõi

### 3.1. Màn hình Dashboard (Trang chủ tĩnh)
File: `ui/screens/dashboard/DashboardScreen.kt`

```kotlin
package com.nutrivision.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrivision.app.ui.theme.*

@Composable
fun DashboardScreen() {
    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header lời chào
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Chào buổi sáng 👋", fontSize = 14.sp, color = TextSecondary)
                        Text(text = "Hôm nay, 04 Th11", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    // Avatar giả lập
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "NV", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }
            }

            // 2. Card Vòng tròn Calo & Macro
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Calo còn lại hôm nay", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "1,250", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text(text = "Mục tiêu: 1,800 kcal", fontSize = 12.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BorderLight)
                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 thanh Macro tóm tắt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroColumn("Đạm", "45g / 110g", MacroProtein)
                            MacroColumn("Carb", "90g / 220g", MacroCarb)
                            MacroColumn("Béo", "25g / 60g", MacroFat)
                        }
                    }
                }
            }

            // 3. Danh sách thẻ 4 bữa ăn
            item {
                Text(text = "Bữa ăn trong ngày", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item { MealCardItem("Bữa Sáng ☀️", "1 Ổ bánh mì thịt (340 kcal)", true) }
            item { MealCardItem("Bữa Trưa 🍲", "Chưa có món ăn", false) }
            item { MealCardItem("Bữa Tối 🌙", "Chưa có món ăn", false) }
            item { MealCardItem("Bữa Phụ ☕", "Chưa có món ăn", false) }
        }
    }
}

@Composable
fun MacroColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
        Text(text = value, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun MealCardItem(mealName: String, status: String, hasItem: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = mealName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(text = status, fontSize = 13.sp, color = if (hasItem) TextPrimary else TextSecondary)
            }
            IconButton(
                onClick = { /* Mở camera hoặc customizer */ },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (hasItem) PrimaryGreen.copy(alpha = 0.1f) else PrimaryGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm món",
                    tint = if (hasItem) PrimaryGreen else Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    NutriVisionTheme {
        DashboardScreen()
    }
}
```

---

### 3.2. Màn hình AI & Tinh chỉnh Khẩu phần (MealCustomizerScreen tĩnh)
File: `ui/screens/customizer/MealCustomizerScreen.kt`

```kotlin
package com.nutrivision.app.ui.screens.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrivision.app.ui.theme.*

@Composable
fun MealCustomizerScreen() {
    Scaffold(
        bottomBar = {
            Button(
                onClick = { /* Lưu vào DB ở Tuần 4 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = "LƯU VÀO BỮA TRƯA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Thẻ kết quả AI
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AccentIndigo.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "✨ AI NHẬN DIỆN MÓN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentIndigo)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Phở bò tái", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Độ tin cậy: 89% • Nhấn để đổi món khác", fontSize = 12.sp, color = TextSecondary)
                }
            }

            // 2. Chọn khẩu phần
            Text(text = "Khẩu phần chính:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = false, onClick = {}, label = { Text("Nhỏ (350g)") })
                FilterChip(selected = true, onClick = {}, label = { Text("Vừa (500g)") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f)))
                FilterChip(selected = false, onClick = {}, label = { Text("Lớn (700g)") })
            }

            // 3. Tinh chỉnh topping
            Text(text = "Tùy chỉnh thêm/bớt:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("+ Thịt bò (50g)") })
                AssistChip(onClick = {}, label = { Text("+ 1 Trứng chần") })
                AssistChip(onClick = {}, label = { Text("- Bớt bánh") })
            }

            // 4. Bảng tính dinh dưỡng thời gian thực
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Tổng dinh dưỡng:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        NutritionStat("Năng lượng", "525 kcal", PrimaryGreen)
                        NutritionStat("Đạm", "32 g", MacroProtein)
                        NutritionStat("Carb", "55 g", MacroCarb)
                        NutritionStat("Béo", "16 g", MacroFat)
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMealCustomizer() {
    NutriVisionTheme {
        MealCustomizerScreen()
    }
}
```

---

## 4. Cách chạy và kiểm tra Layout trên Android Studio
1. Mở file `DashboardScreen.kt` hoặc `MealCustomizerScreen.kt`.
2. Bật cửa sổ **Split / Design** ở góc trên bên phải Android Studio.
3. Chờ Compose Preview render là có thể xem trước giao diện và chụp ảnh màn hình báo cáo nộp cho giảng viên mà chưa cần kết nối Database hay logic phức tạp!

