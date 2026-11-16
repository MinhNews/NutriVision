package com.example.nutrivision.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.text.tr
import com.example.nutrivision.ui.components.DotsLoading
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SecondaryAmber
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.viewmodel.CameraUiState
import com.example.nutrivision.ui.viewmodel.CameraViewModel
import com.example.nutrivision.ui.viewmodel.PhotoCandidate
import com.example.nutrivision.ui.viewmodel.RecognizeStage
import com.example.nutrivision.ui.util.oneDecimal
import com.example.nutrivision.ui.util.SystemBarsAppearance
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Tab "Chụp món": CameraX thật + model TFLite chạy trên máy.
 *
 * Ảnh được ghi vào filesDir/photos, truyền cho CameraViewModel nhận diện, rồi
 * người dùng xem lại khẩu phần và bấm lưu — không có bước nào ghi thẳng vào DB.
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    uiState: CameraUiState,
    onOpenSearch: () -> Unit,
    onOpenDiary: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    // Chọn ảnh có sẵn: cùng pipeline với ảnh chụp (model trên máy → Gemini nếu cần).
    // Photo Picker không cần quyền đọc thư viện, chỉ trả về đúng ảnh người dùng chọn.
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val path = importPickedPhoto(context, uri)
            if (path != null) {
                viewModel.onPhotoCaptured(path)
            } else {
                viewModel.onPhotoImportFailed()
            }
        }
    }
    val pickPhoto = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        Unit
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    val showCamera = uiState.stage == RecognizeStage.IDLE && hasPermission

    // Camera chạy nền tối nên icon trắng; màn kết quả nền sáng nên phải đổi lại icon tối.
    SystemBarsAppearance(useDarkIcons = !showCamera)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (showCamera) {
                    Color(0xFF031F17)
                } else {
                    MaterialTheme.colorScheme.background
                }
            ),
    ) {
        when {
            !hasPermission -> PermissionRequest(
                onRequest = { launcher.launch(Manifest.permission.CAMERA) },
                onOpenSearch = onOpenSearch,
                onPickPhoto = pickPhoto,
            )

            showCamera -> CameraCapture(
                viewModel = viewModel,
                onOpenSearch = onOpenSearch,
                onPickPhoto = pickPhoto,
            )

            uiState.stage == RecognizeStage.ANALYZING -> AnalyzingState(uiState.photoPath)

            else -> RecognitionResult(
                uiState = uiState,
                aiConfigured = viewModel.aiConfigured,
                onSelect = viewModel::selectCandidate,
                onAskAi = viewModel::askAi,
                onGrams = viewModel::setGrams,
                onMealType = viewModel::setMealType,
                onSave = viewModel::save,
                onRetake = { viewModel.reset() },
                onOpenSearch = onOpenSearch,
                onOpenDiary = onOpenDiary,
                onDismissMessage = viewModel::clearMessage,
            )
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequest: () -> Unit,
    onOpenSearch: () -> Unit,
    onPickPhoto: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MintMid),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.CameraAlt,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            tr("Cần quyền dùng camera", "Camera permission needed"),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            tr(
                "NutriVision chỉ dùng camera khi bạn bấm chụp. Ảnh nằm trên máy, chỉ gửi lên Gemini khi bạn bấm \"Nhờ AI mô tả\".",
                "NutriVision only uses the camera when you take a photo. Photos stay on this device unless you tap \"Ask AI to describe\".",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = tr("Cho phép camera", "Allow camera"),
            onClick = onRequest,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onOpenSearch) {
            Text(tr("Tìm món bằng danh mục", "Search the catalogue instead"), color = PrimaryGreen)
        }
        TextButton(onClick = onPickPhoto) {
            Icon(
                Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(tr("Chọn ảnh có sẵn", "Choose a photo"), color = PrimaryGreen)
        }
    }
}

@Composable
private fun CameraCapture(
    viewModel: CameraViewModel,
    onOpenSearch: () -> Unit,
    onPickPhoto: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    var capturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        future.addListener({
            val cameraProvider = future.get()
            provider = cameraProvider
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture,
                )
                imageCapture = capture
            }.onFailure {
                captureError = tr(
                    "Không mở được camera trên thiết bị này.",
                    "Could not open the camera on this device.",
                )
            }
        }, executor)

        onDispose {
            provider?.unbindAll()
            provider = null
            imageCapture = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            tr("Chụp món để nhận diện", "Snap a dish to recognise it"),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Text(
            tr(
                "Đặt món vào giữa khung, đủ sáng, chụp từ trên xuống.",
                "Frame the dish in the middle, good light, shoot from above.",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f),
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(32.dp)),
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
            ScannerFrame()
            if (capturing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    DotsLoading(color = Color.White, dotSize = 9.dp)
                }
            }
        }

        captureError?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFCA5A5))
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.IconButton(
                    enabled = imageCapture != null && !capturing,
                    onClick = {
                        val capture = imageCapture ?: return@IconButton
                        capturing = true
                        captureError = null
                        val dir = File(context.filesDir, "photos").apply { mkdirs() }
                        val file = File(dir, "${UUID.randomUUID()}.jpg")
                        val options = ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    capturing = false
                                    viewModel.onPhotoCaptured(file.absolutePath)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    capturing = false
                                    captureError = tr(
                                        "Chụp ảnh thất bại: ${exception.message ?: "lỗi không rõ"}",
                                        "Capture failed: ${exception.message ?: "unknown error"}",
                                    )
                                }
                            },
                        )
                    },
                ) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        contentDescription = tr("Chụp ảnh", "Take photo"),
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onOpenSearch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                tr("Không chụp được? Tìm món trong danh mục", "Can't shoot? Search the catalogue"),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        TextButton(onClick = onPickPhoto, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                tr("Chọn ảnh có sẵn", "Choose a photo"),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

/**
 * Đọc ảnh người dùng chọn từ thư viện, thu nhỏ còn tối đa 1600 px và lưu thành JPEG
 * trong bộ nhớ riêng của app — để model trên máy và Gemini dùng chung một đường xử lý.
 * Trả về null khi ảnh hỏng hoặc không đọc được.
 */
private suspend fun importPickedPhoto(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            file.absolutePath
        }.getOrNull()
    }

@Composable
private fun ScannerFrame() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .border(2.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(26.dp)),
        )
    }
}

@Composable
private fun AnalyzingState(photoPath: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )
            Spacer(Modifier.height(22.dp))
        }
        DotsLoading(color = PrimaryGreen, dotSize = 10.dp)
        Spacer(Modifier.height(14.dp))
        Text(
            tr("Đang phân tích ảnh trên máy...", "Analysing the photo on-device..."),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tr("Không cần mạng cho bước này.", "This step needs no network."),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun RecognitionResult(
    uiState: CameraUiState,
    aiConfigured: Boolean,
    onSelect: (PhotoCandidate) -> Unit,
    onAskAi: () -> Unit,
    onGrams: (Double) -> Unit,
    onMealType: (MealType) -> Unit,
    onSave: () -> Unit,
    onRetake: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDiary: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    val food = uiState.food
    val saved = uiState.savedItemId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                tr("Kết quả nhận diện", "Recognition result"),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetake) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(tr("Chụp lại", "Retake"), color = PrimaryGreen)
            }
        }

        uiState.photoPath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(26.dp)),
            )
            Spacer(Modifier.height(14.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (food != null) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = ForestInk,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfidenceChip(uiState)
                    if (uiState.source == MealItemEntity.SOURCE_GEMINI) {
                        Chip(
                            text = "AI",
                            background = SecondaryAmber.copy(alpha = 0.18f),
                            contentColor = Color(0xFF92400E),
                        )
                    }
                }
                if (!uiState.confident) {
                    Text(
                        tr(
                            "Model chưa chắc chắn. Bạn có thể chọn món gần nhất bên dưới, nhờ AI mô tả, hoặc tìm trong danh mục.",
                            "The model isn't sure. Pick the closest match below, ask AI, or search the catalogue.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            } else {
                Text(
                    tr("Chưa nhận ra món này", "Could not recognise this dish"),
                    style = MaterialTheme.typography.titleLarge,
                    color = ForestInk,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    tr(
                        "Thử chụp lại rõ hơn, nhờ AI mô tả, hoặc tìm món trong danh mục.",
                        "Try a clearer photo, ask AI, or search the catalogue.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            if (food != null) {
                val nutrition = food.nutritionFor(uiState.grams)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroBox(
                        label = tr("Calo", "Calories"),
                        value = nutrition.calories.roundToInt().toString(),
                        unit = "kcal",
                        modifier = Modifier.weight(1f),
                    )
                    MacroBox(
                        label = tr("Đạm", "Protein"),
                        value = nutrition.protein.oneDecimal(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                    MacroBox(
                        label = tr("Carb", "Carbs"),
                        value = nutrition.carbs.oneDecimal(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                    MacroBox(
                        label = tr("Béo", "Fat"),
                        value = nutrition.fat.oneDecimal(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Khẩu phần", "Portion"),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    GramStepper(grams = uiState.grams, onGrams = onGrams)
                }
                Text(
                    tr(
                        "${uiState.grams.roundToInt()} g · gốc ${food.portionName}",
                        "${uiState.grams.roundToInt()} g · base ${food.portionName}",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.mealType == type,
                            onClick = { onMealType(type) },
                            label = { Text(type.shortLabel, style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
            }

            val alternatives = uiState.candidates.filter { it.foodId != null && it.foodId != food?.id }
            if (alternatives.isNotEmpty() && !saved) {
                Text(
                    tr("Có thể là:", "Might also be:"),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    alternatives.take(2).forEach { candidate ->
                        FilterChip(
                            selected = false,
                            onClick = { onSelect(candidate) },
                            label = {
                                Text(
                                    "${candidate.name} · ${candidate.percent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.message != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                uiState.message?.let { message ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.confident) TextSecondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onDismissMessage) {
                            Text(tr("Đóng", "Close"), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            if (saved) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tr("Đã lưu vào nhật ký hôm nay", "Saved to today's diary"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestInk,
                    )
                }
                GradientButton(
                    text = tr("Xem nhật ký", "Open diary"),
                    onClick = onOpenDiary,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                if (!uiState.confident && aiConfigured && (food == null || uiState.source != MealItemEntity.SOURCE_GEMINI)) {
                    GradientButton(
                        text = tr("Nhờ AI mô tả", "Ask AI to describe"),
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = onAskAi,
                        loading = uiState.aiBusy,
                        enabled = !uiState.aiBusy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                GradientButton(
                    text = tr("Lưu vào nhật ký", "Save to diary"),
                    onClick = onSave,
                    enabled = uiState.canSave && !uiState.aiBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = onOpenSearch) {
                        Text(tr("Tìm món khác", "Find another dish"), color = PrimaryGreen)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ConfidenceChip(uiState: CameraUiState) {
    val confidence = uiState.confidence
    val text = when {
        uiState.source == MealItemEntity.SOURCE_GEMINI -> tr("AI ước lượng", "AI estimate")
        confidence == null -> tr("Chưa rõ", "Unknown")
        else -> tr(
            "Độ tin cậy ${(confidence * 100).roundToInt()}%",
            "Confidence ${(confidence * 100).roundToInt()}%",
        )
    }
    Chip(
        text = text,
        background = if (uiState.confident) MintMid else SecondaryAmber.copy(alpha = 0.2f),
        contentColor = if (uiState.confident) ForestInk else Color(0xFF92400E),
    )
}

@Composable
private fun Chip(text: String, background: Color, contentColor: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun MacroBox(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MintMid.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = ForestInk)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun GramStepper(grams: Double, onGrams: (Double) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton(text = "−50", onClick = { onGrams(grams - 50) })
        Text(
            "${grams.roundToInt()} g",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        StepButton(text = "+50", onClick = { onGrams(grams + 50) })
    }
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = PrimaryGreen)
    }
}

/** Hiển thị tên món khi cần (dùng lại ở nơi khác nếu muốn). */
internal fun Food.displayNameOrId(): String = name.ifBlank { id }
