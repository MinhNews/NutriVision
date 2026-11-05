package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.theme.*

@Composable
fun DashboardScreen(onScanClick: () -> Unit = {}) {
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
            // 1. Header chào mừng
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Chào buổi sáng 👋", fontSize = 14.sp, color = TextSecondary)
                        Text(text = "Hôm nay, 05 Th11", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(21.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "NV", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }
            }

            // 2. Khung thẻ Calo tổng quan
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
                        Text(text = "Calo còn lại hôm nay", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "1,250", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text(text = "Mục tiêu: 1,800 kcal", fontSize = 12.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderLight)
                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Cột Macro
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MacroStatItem("Đạm", "45g / 110g", MacroProtein)
                            MacroStatItem("Carb", "90g / 220g", MacroCarb)
                            MacroStatItem("Béo", "25g / 60g", MacroFat)
                        }
                    }
                }
            }

            // 3. Khung danh sách 4 bữa ăn
            item {
                Text(text = "Bữa ăn trong ngày", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item { MealStaticCard("Bữa Sáng ☀️", "1 Ổ bánh mì thịt (340 kcal)", isDone = true) }
            item { MealStaticCard("Bữa Trưa 🍲", "Chưa có món ăn", isDone = false, onClick = onScanClick) }
            item { MealStaticCard("Bữa Tối 🌙", "Chưa có món ăn", isDone = false) }
            item { MealStaticCard("Bữa Phụ ☕", "Chưa có món ăn", isDone = false) }
        }
    }
}

@Composable
fun MacroStatItem(name: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
        Text(text = value, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun MealStaticCard(title: String, subtitle: String, isDone: Boolean, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(text = subtitle, fontSize = 13.sp, color = if (isDone) TextPrimary else TextSecondary)
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) PrimaryGreen.copy(alpha = 0.15f) else PrimaryGreen,
                    contentColor = if (isDone) PrimaryGreen else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = if (isDone) "Đã ghi" else "+ Thêm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen()
}
