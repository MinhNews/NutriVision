package com.example.nutrivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.nutrivision.ui.screens.*
import com.example.nutrivision.ui.theme.NutriVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriVisionTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    // Trạng thái đăng nhập giả lập: Chưa đăng nhập -> hiện AuthScreen; Đăng nhập rồi -> hiện BottomNav
    var isLoggedIn by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    if (!isLoggedIn) {
        AuthScreen(onLoginSuccess = { isLoggedIn = true })
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Trang chủ") },
                        icon = { Text("🏠") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Quét món") },
                        icon = { Text("📷") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Chi tiết") },
                        icon = { Text("🍲") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Hồ sơ") },
                        icon = { Text("👤") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> DashboardScreen(onScanClick = { selectedTab = 1 })
                    1 -> CameraScreen(onCaptureClick = { selectedTab = 2 })
                    2 -> MealDetailScreen(onSaveClick = { selectedTab = 0 })
                    3 -> ProfileScreen(onLogoutClick = { isLoggedIn = false })
                }
            }
        }
    }
}
