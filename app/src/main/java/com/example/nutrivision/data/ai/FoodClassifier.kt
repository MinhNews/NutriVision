package com.example.nutrivision.data.ai

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/** Một dự đoán của model: nhãn (slug món ăn) + độ tin cậy 0..1. */
data class FoodPrediction(
    val label: String,
    val confidence: Float,
)

/**
 * Bọc model.tflite (MobileNetV3, 224×224, 131 lớp) chạy hoàn toàn trên máy.
 *
 * Quan trọng: đầu vào là float32 trong khoảng 0..255, **không** chia 255 —
 * bước chuẩn hoá đã nằm sẵn trong model. Chia 255 sẽ làm model đoán sai hoàn toàn
 * (đã đối chiếu với test_ai/predict.py, top-1 71,2%).
 */
class FoodClassifier(context: Context) : Closeable {

    private val appContext = context.applicationContext
    private val lock = Any()

    private val interpreterLazy = lazy {
        val options = Interpreter.Options().apply { setNumThreads(2) }
        Interpreter(loadModelFile(), options)
    }

    private val interpreter: Interpreter by interpreterLazy

    /** Thứ tự nhãn khớp đúng chỉ số đầu ra của model. */
    val labels: List<String> by lazy {
        appContext.assets.open(LABELS_ASSET).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    }

    /** Tên tiếng Việt dự phòng khi món chưa có trong bảng foods. */
    private val displayNames: Map<String, String> by lazy {
        runCatching {
            val json = appContext.assets.open(CLASS_NAMES_ASSET)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val obj = org.json.JSONObject(json)
            obj.keys().asSequence().associateWith { key -> obj.optString(key) }
        }.getOrDefault(emptyMap())
    }

    fun displayName(label: String): String =
        displayNames[label] ?: label.replace('_', ' ')

    /**
     * Chạy nhận diện trên ảnh đã chụp. Trả về tối đa [topK] dự đoán, sắp xếp giảm dần.
     * Không ném exception ra ngoài: lỗi model trả về danh sách rỗng để UI báo "chưa nhận ra".
     */
    fun classify(bitmap: Bitmap, topK: Int = 3): List<FoodPrediction> = synchronized(lock) {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(labels.size) }
        runCatching { interpreter.run(input, output) }.getOrElse { return emptyList() }

        val scores = output[0]
        labels.indices
            .sortedByDescending { scores[it] }
            .take(topK)
            .map { FoodPrediction(labels[it], scores[it]) }
    }

    /**
     * Resize 224×224 (bilinear) rồi đổ RGB vào ByteBuffer float32 0..255 theo layout NHWC.
     */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val scaled = if (bitmap.width == INPUT_SIZE && bitmap.height == INPUT_SIZE) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        }
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        if (scaled !== bitmap) scaled.recycle()

        val buffer = ByteBuffer
            .allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
            .order(ByteOrder.nativeOrder())
        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF).toFloat())
            buffer.putFloat(((pixel shr 8) and 0xFF).toFloat())
            buffer.putFloat((pixel and 0xFF).toFloat())
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(): MappedByteBuffer {
        appContext.assets.openFd(MODEL_ASSET).use { fd ->
            FileInputStream(fd.fileDescriptor).use { stream ->
                return stream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength,
                )
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            if (interpreterLazy.isInitialized()) runCatching { interpreter.close() }
        }
    }

    companion object {
        const val INPUT_SIZE = 224
        const val MODEL_ASSET = "model.tflite"
        const val LABELS_ASSET = "labels.txt"
        const val CLASS_NAMES_ASSET = "class_names_vi.json"

        /** Ngưỡng tin cậy lấy từ model_meta.json. */
        const val CONFIDENCE_THRESHOLD = 0.7f
    }
}
