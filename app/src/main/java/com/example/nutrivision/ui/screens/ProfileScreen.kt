package com.example.nutrivision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.theme.*

@Composable
fun ProfileScreen(onLogoutClick: () -> Unit = {}) {
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("65") }
    var age by remember { mutableStateOf("22") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Hồ sơ thể trạng (BMI / TDEE)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Dữ liệu để tính toán mục tiêu Calo phù hợp cho bạn", fontSize = 13.sp, color = TextSecondary)

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Chiều cao (cm)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Cân nặng (kg)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Độ tuổi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(text = "CẬP NHẬT HỒ SƠ", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "ĐĂNG XUẤT", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    ProfileScreen()
}
