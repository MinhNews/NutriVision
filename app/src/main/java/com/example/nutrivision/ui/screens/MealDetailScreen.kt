package com.example.nutrivision.ui.screens

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
import com.example.nutrivision.ui.theme.*

@Composable
fun MealDetailScreen(onSaveClick: () -> Unit = {}) {
    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = "LƯU VÀO BỮA ĂN", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                    Text(text = "Phở bò tái nạm", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Độ tin cậy: 89% • Chạm để đổi món", fontSize = 12.sp, color = TextSecondary)
                }
            }

            // 2. Chọn khẩu phần
            Text(text = "Khẩu phần chính:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = false, onClick = {}, label = { Text("Nhỏ (350g)") })
                FilterChip(selected = true, onClick = {}, label = { Text("Vừa (500g)") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f)))
                FilterChip(selected = false, onClick = {}, label = { Text("Lớn (700g)") })
            }

            // 3. Khung tinh chỉnh Topping
            Text(text = "Tùy chỉnh thêm/bớt:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("+ Thịt bò (50g)") })
                AssistChip(onClick = {}, label = { Text("+ 1 Trứng chần") })
                AssistChip(onClick = {}, label = { Text("- Bớt bánh") })
            }

            // 4. Bảng tính dinh dưỡng
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Tổng dinh dưỡng:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        NutriStatItem("Năng lượng", "450 kcal", PrimaryGreen)
                        NutriStatItem("Đạm", "25 g", MacroProtein)
                        NutriStatItem("Carb", "55 g", MacroCarb)
                        NutriStatItem("Béo", "14 g", MacroFat)
                    }
                }
            }
        }
    }
}

@Composable
fun NutriStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMealDetail() {
    MealDetailScreen()
}
