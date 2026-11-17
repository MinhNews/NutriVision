package com.example.nutrivision.data.auth

import com.example.nutrivision.domain.text.tr
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Ket qua cua mot thao tac xac thuc: khong nem exception ra UI. */
sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * Lop bao FirebaseAuth. Toan bo phien dang nhap do Firebase giu (tu dong luu qua lan mo app),
 * khong con co "logged_in" gia trong SharedPreferences nhu tuan 3.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    val currentUserId: String? get() = auth.currentUser?.uid

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult =
        runAuth {
            val result = auth.createUserWithEmailAndPassword(email, password).awaitTask()
            val user = result.user ?: error("Firebase khong tra ve user")
            if (displayName.isNotBlank()) {
                user.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                ).awaitTask()
            }
            // Gui link xac thuc; loi gui mail khong lam hong buoc dang ky.
            runCatching { user.sendEmailVerification().awaitTask() }
            AuthResult.Success
        }

    suspend fun login(email: String, password: String): AuthResult =
        runAuth {
            auth.signInWithEmailAndPassword(email, password).awaitTask()
            AuthResult.Success
        }

    suspend fun resetPassword(email: String): AuthResult =
        runAuth {
            auth.sendPasswordResetEmail(email).awaitTask()
            AuthResult.Success
        }

    suspend fun resendVerification(): AuthResult = runAuth {
        val user = auth.currentUser ?: return@runAuth AuthResult.Failure(
            tr("Chưa đăng nhập", "Not signed in")
        )
        user.sendEmailVerification().awaitTask()
        AuthResult.Success
    }

    /** Doc lai trang thai xac thuc email tu server (sau khi nguoi dung bam link). */
    suspend fun reloadUser(): Boolean = runCatching {
        val user = auth.currentUser ?: return false
        user.reload().awaitTask()
        user.isEmailVerified
    }.getOrDefault(false)

    /** Xoa tai khoan Firebase. Can dang nhap lai gan day, neu khong Firebase tra loi rieng. */
    suspend fun deleteAccount(): AuthResult = runAuth {
        val user = auth.currentUser ?: return@runAuth AuthResult.Failure(
            tr("Chưa đăng nhập", "Not signed in")
        )
        user.delete().awaitTask()
        AuthResult.Success
    }

    fun signOut() = auth.signOut()

    private suspend fun runAuth(block: suspend () -> AuthResult): AuthResult = try {
        block()
    } catch (e: Exception) {
        AuthResult.Failure(describe(e))
    }

    private fun describe(error: Throwable): String = when (error) {
        is FirebaseNetworkException -> tr(
            "Không có kết nối mạng. Kiểm tra Wi-Fi/4G rồi thử lại.",
            "No network connection. Check Wi-Fi/mobile data and try again.",
        )
        is FirebaseAuthUserCollisionException -> tr(
            "Email này đã có tài khoản. Hãy đăng nhập hoặc dùng email khác.",
            "This email already has an account. Sign in or use another email.",
        )
        is FirebaseAuthWeakPasswordException -> tr(
            "Mật khẩu quá yếu, cần ít nhất 6 ký tự.",
            "Password is too weak, use at least 6 characters.",
        )
        is FirebaseAuthInvalidUserException -> when (error.errorCode) {
            "ERROR_USER_DISABLED" -> tr(
                "Tài khoản đã bị khóa.",
                "This account has been disabled.",
            )
            else -> tr(
                "Email này chưa có tài khoản. Hãy đăng ký.",
                "No account for this email. Please sign up.",
            )
        }
        is FirebaseAuthInvalidCredentialsException -> tr(
            "Email hoặc mật khẩu không đúng.",
            "Email or password is incorrect.",
        )
        is FirebaseAuthRecentLoginRequiredException -> tr(
            "Cần đăng nhập lại để thực hiện thao tác này.",
            "Please sign in again to continue.",
        )
        is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_INVALID_EMAIL" -> tr("Email chưa đúng định dạng.", "Email format is invalid.")
            "ERROR_OPERATION_NOT_ALLOWED" -> tr(
                "Chưa bật đăng nhập bằng Email/Password trong Firebase.",
                "Email/Password sign-in is not enabled in Firebase.",
            )
            "ERROR_TOO_MANY_REQUESTS" -> tr(
                "Thử quá nhiều lần. Đợi một lát rồi thử lại.",
                "Too many attempts. Wait a moment and try again.",
            )
            else -> error.localizedMessage ?: tr("Đã có lỗi xảy ra.", "Something went wrong.")
        }
        else -> error.localizedMessage ?: tr("Đã có lỗi xảy ra.", "Something went wrong.")
    }
}

/** Cho Task cua Google Play Services thanh suspend ma khong can them kotlinx-coroutines-play-services. */
internal suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWithException(task.exception ?: IllegalStateException("Task failed"))
        }
    }
}
