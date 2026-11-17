package com.example.nutrivision.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nutrivision.R
import com.example.nutrivision.domain.text.tr
import com.example.nutrivision.domain.validation.AuthIssue
import com.example.nutrivision.domain.validation.AuthValidation
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.AppTextField
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.viewmodel.AuthMessage

/**
 * Màn đăng nhập / đăng ký thật (Firebase Auth email + mật khẩu).
 * Không còn chế độ "bấm là vào": sai mật khẩu, email trùng, mất mạng đều báo lỗi thật.
 */
@Composable
fun AuthScreen(
    busy: Boolean,
    message: AuthMessage?,
    onClearMessage: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, displayName: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
) {
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var issue by rememberSaveable { mutableStateOf<AuthIssue?>(null) }
    var showTerms by rememberSaveable { mutableStateOf(false) }

    fun switchMode(register: Boolean) {
        isRegister = register
        issue = null
        password = ""
        confirmPassword = ""
        onClearMessage()
    }

    fun submit() {
        val found = AuthValidation.check(
            email = email,
            password = password,
            displayName = name,
            confirmPassword = confirmPassword,
            isRegister = isRegister,
            termsAccepted = termsAccepted,
        )
        issue = found
        if (found != null) return
        onClearMessage()
        if (isRegister) {
            onRegister(email.trim(), password, name.trim())
        } else {
            onLogin(email.trim(), password)
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
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "NutriVision",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(17.dp)),
                )
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
                        if (register) {
                            tr("Bắt đầu hành trình", "Start your journey")
                        } else {
                            tr("Chào mừng trở lại", "Welcome back")
                        },
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (register) {
                            tr(
                                "Tạo tài khoản để lưu nhật ký và đồng bộ trên nhiều máy.",
                                "Create an account to keep your diary and sync across devices.",
                            )
                        } else {
                            tr(
                                "Đăng nhập để tiếp tục ghi bữa ăn hôm nay.",
                                "Sign in to keep logging today's meals.",
                            )
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
                        onValueChange = { name = it; issue = null },
                        label = tr("Tên hiển thị", "Display name"),
                        placeholder = tr("Ví dụ: Minh Anh", "e.g. Minh Anh"),
                        leadingIcon = Icons.Rounded.Person,
                        isError = issue == AuthIssue.NAME_MISSING,
                    )
                }
                AppTextField(
                    value = email,
                    onValueChange = { email = it; issue = null; onClearMessage() },
                    label = "Email",
                    placeholder = "ban@example.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = issue == AuthIssue.EMAIL_INVALID,
                )
                AppTextField(
                    value = password,
                    onValueChange = { password = it; issue = null; onClearMessage() },
                    label = tr("Mật khẩu", "Password"),
                    placeholder = tr("Ít nhất 6 ký tự", "At least 6 characters"),
                    leadingIcon = Icons.Rounded.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    tr("Ẩn mật khẩu", "Hide password")
                                } else {
                                    tr("Hiện mật khẩu", "Show password")
                                },
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    isError = issue == AuthIssue.PASSWORD_TOO_SHORT,
                )
                if (isRegister) {
                    AppTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; issue = null },
                        label = tr("Nhập lại mật khẩu", "Confirm password"),
                        leadingIcon = Icons.Rounded.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = issue == AuthIssue.PASSWORD_MISMATCH,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted; issue = null },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it; issue = null },
                        )
                        Text(
                            tr(
                                "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật",
                                "I agree to the Terms of Use and Privacy Policy",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    TextButton(
                        onClick = { showTerms = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(
                            tr("Xem điều khoản", "Read the terms"),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen,
                        )
                    }
                }

                if (!isRegister) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = { onForgotPassword(email.trim()) },
                            enabled = !busy,
                        ) {
                            Text(
                                tr("Quên mật khẩu?", "Forgot password?"),
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryGreen,
                            )
                        }
                    }
                }

                val shownMessage = issue?.let { authIssueMessage(it) } ?: message?.text
                if (shownMessage != null) {
                    Text(
                        shownMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (issue != null || message?.isError == true) {
                            MaterialTheme.colorScheme.error
                        } else {
                            PrimaryGreen
                        },
                    )
                }

                GradientButton(
                    text = if (isRegister) {
                        tr("Tạo tài khoản", "Create account")
                    } else {
                        tr("Đăng nhập", "Sign in")
                    },
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth(),
                    loading = busy,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isRegister) {
                            tr("Đã có tài khoản?", "Already have an account?")
                        } else {
                            tr("Chưa có tài khoản?", "No account yet?")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    TextButton(onClick = { switchMode(!isRegister) }) {
                        Text(
                            if (isRegister) {
                                tr("Đăng nhập", "Sign in")
                            } else {
                                tr("Đăng ký ngay", "Sign up")
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryGreen,
                        )
                    }
                }
            }


            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = {
                Text(tr("Điều khoản sử dụng & Chính sách bảo mật", "Terms of Use & Privacy Policy"))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        tr("Cập nhật: 15/11/2026. Phiên bản 1.", "Updated: 15/11/2026. Version 1."),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                    Text(
                        tr(
                            "1. Dữ liệu: hồ sơ, nhật ký bữa ăn và ảnh món ăn được lưu trên thiết bị và trong tài khoản Firebase của bạn.",
                            "1. Data: your profile, meal diary and food photos are stored on this device and in your own Firebase account.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        tr(
                            "2. Ảnh chỉ được gửi ra ngoài khi bạn bấm \"Nhờ AI mô tả\" (Google Gemini). Không gửi ngầm.",
                            "2. Photos leave the device only when you tap \"Ask AI to describe\" (Google Gemini). Nothing is uploaded silently.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        tr(
                            "3. Số liệu dinh dưỡng chỉ mang tính tham khảo, không thay thế tư vấn y tế.",
                            "3. Nutrition numbers are for reference only and do not replace medical advice.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        tr(
                            "4. Bạn có thể xóa toàn bộ dữ liệu bất cứ lúc nào trong tab Cá nhân > Xóa tài khoản.",
                            "4. You can delete all your data any time in Profile > Delete account.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        tr(
                            "5. Chúng tôi không bán dữ liệu cá nhân của bạn.",
                            "5. We do not sell your personal data.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    termsAccepted = true
                    issue = null
                    showTerms = false
                }) {
                    Text(tr("Đồng ý", "Agree"), color = PrimaryGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerms = false }) {
                    Text(tr("Đóng", "Close"), color = TextSecondary)
                }
            },
        )
    }
}

private fun authIssueMessage(issue: AuthIssue): String = when (issue) {
    AuthIssue.EMAIL_INVALID -> tr("Email chưa đúng định dạng", "Email format looks wrong")
    AuthIssue.PASSWORD_TOO_SHORT -> tr(
        "Mật khẩu cần ít nhất 6 ký tự",
        "Password needs at least 6 characters",
    )
    AuthIssue.NAME_MISSING -> tr("Hãy nhập tên hiển thị", "Enter a display name")
    AuthIssue.PASSWORD_MISMATCH -> tr(
        "Hai lần nhập mật khẩu chưa khớp",
        "Passwords do not match",
    )
    AuthIssue.TERMS_NOT_ACCEPTED -> tr(
        "Bạn cần đồng ý Điều khoản sử dụng để tiếp tục",
        "Accept the Terms of Use to continue",
    )
}
