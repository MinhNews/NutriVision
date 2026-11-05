package com.example.nutrivision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.nutrivision.ui.theme.PrimaryGreen

@Composable
fun CameraScreen(onCaptureClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Khung ngắm chụp ảnh (Viewfinder)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .border(2.dp, PrimaryGreen, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đặt món ăn vào giữa khung hình",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Thanh thao tác dưới đáy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Thư viện
            Button(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Text(text = "🖼️ Thư viện", fontSize = 12.sp)
            }

            // Nút Chụp tròn
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onCaptureClick) {
                    Box(modifier = Modifier.size(40.dp))
                }
            }

            // Nút Flash
            Button(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Text(text = "⚡ Flash", fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCamera() {
    CameraScreen()
}
