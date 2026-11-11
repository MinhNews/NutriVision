package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.AppTextField
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    profileExists: Boolean,
    onLogin: () -> Unit,
    onRegister: (email: String, displayName: String) -> Unit,
) {
    var isRegister by rememberSaveable { mutableStateOf(!profileExists) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun switchMode(register: Boolean) {
        isRegister = register
        error = null
        password = ""
        confirmPassword = ""
    }

    fun submit() {
        val cleanEmail = email.trim()
        error = when {
            !cleanEmail.contains("@") || cleanEmail.length < 5 -> tr("Email chưa đúng định dạng", "Email format looks wrong")
            password.length < 6 -> tr("Mật khẩu cần ít nhất 6 ký tự", "Password needs at least 6 characters")
            isRegister && name.trim().length < 2 -> tr("Hãy nhập tên hiển thị", "Enter a display name")
            isRegister && password != confirmPassword -> tr("Hai lần nhập mật khẩu chưa khớp", "Passwords do not match")
            else -> null
        }
        if (error != null) return
        if (isRegister) {
            onRegister(cleanEmail, name.trim())
        } else {
            if (profileExists) onLogin() else {
                error = tr("Máy này chưa có hồ sơ. Hãy tạo tài khoản để bắt đầu.", "No profile on this device yet. Create an account to start.")
                isRegister = true
            }
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Brush.linearGradient(listOf(PrimaryGreen, ForestDeep))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Spa, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "NutriVision",
                        style = MaterialTheme.typography.titleLarge,
                        color = ForestInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        tr("Nhật ký dinh dưỡng thông minh", "Smart nutrition diary"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(38.dp))

            AnimatedContent(
                targetState = isRegister,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "authMode",
            ) { register ->
                Column {
                    Text(
                        if (register) tr("Bắt đầu hành trình", "Start your journey") else tr("Chào mừng trở lại", "Welcome back"),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (register) {
                            tr("Tạo hồ sơ để app tính mục tiêu calo và dinh dưỡng riêng cho bạn.", "Create a profile so the app can calculate your own calorie and nutrition targets.")
                        } else {
                            tr("Đăng nhập để tiếp tục ghi bữa ăn hôm nay.", "Sign in to keep logging today's meals.")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceLight)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isRegister) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = tr("Tên hiển thị", "Display name"),
                        placeholder = tr("Ví dụ: Minh Anh", "e.g. Minh Anh"),
                        leadingIcon = Icons.Rounded.Person,
                    )
                }
                AppTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = "Email",
                    placeholder = "ban@example.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    isError = error?.contains("Email") == true,
                )
                AppTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = tr("Mật khẩu", "Password"),
                    placeholder = tr("Ít nhất 6 ký tự", "At least 6 characters"),
                    leadingIcon = Icons.Rounded.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (passwordVisible) tr("Ẩn mật khẩu", "Hide password") else tr("Hiện mật khẩu", "Show password"),
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    isError = error?.contains(tr("Mật khẩu", "Password")) == true && !isRegister,
                )
                if (isRegister) {
                    AppTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = null },
                        label = tr("Nhập lại mật khẩu", "Confirm password"),
                        leadingIcon = Icons.Rounded.Lock,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = error?.contains(tr("chưa khớp", "do not match")) == true,
                    )
                }

                if (error != null) {
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(4.dp))
                GradientButton(
                    text = if (isRegister) tr("Tạo tài khoản", "Create account") else tr("Đăng nhập", "Sign in"),
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!isRegister && !profileExists) {
                    Text(
                        tr("Thiết bị này chưa có tài khoản. Hãy tạo tài khoản mới.", "No account on this device yet. Create a new one."),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isRegister) tr("Đã có tài khoản?", "Already have an account?") else tr("Chưa có tài khoản?", "No account yet?"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    TextButton(onClick = { switchMode(!isRegister) }) {
                        Text(
                            if (isRegister) tr("Đăng nhập", "Sign in") else tr("Đăng ký ngay", "Sign up"),
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryGreen,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MintMid.copy(alpha = 0.55f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(top = 4.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    tr("Thông tin cá nhân và nhật ký bữa ăn của bạn luôn được bảo mật.", "Your profile and meal diary stay private."),
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestInk.copy(alpha = 0.8f),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
