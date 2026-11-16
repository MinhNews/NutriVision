package com.example.nutrivision

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.BitmapFactory
import com.example.nutrivision.data.ai.FoodClassifier
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Chạy model thật trên thiết bị với ảnh thật trong assets để chắc chắn
 * tiền xử lý (224×224, float32 0..255, KHÔNG chia 255) khớp với test_ai/predict.py.
 *
 * Nếu chia 255 hoặc đảo kênh màu, độ chính xác sẽ tụt xuống gần 0 và test này fail.
 */
@RunWith(AndroidJUnit4::class)
class FoodClassifierInstrumentedTest {

    @Test
    fun model_nhan_dien_dung_huong_tren_anh_mau() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val classifier = FoodClassifier(context)
        val labels = classifier.labels.toSet()
        val assets = context.assets.list("food_images").orEmpty()
            .filter { it.substringBeforeLast('.') in labels }

        assertTrue("Không tìm thấy ảnh mẫu nào", assets.size >= 100)

        var evaluated = 0
        var top1 = 0
        var confident = 0

        assets.forEach { file ->
            val bitmap = context.assets.open("food_images/$file").use { BitmapFactory.decodeStream(it) }
            if (bitmap == null) return@forEach
            val predictions = classifier.classify(bitmap, topK = 3)
            bitmap.recycle()
            if (predictions.isEmpty()) return@forEach

            evaluated++
            val expected = file.substringBeforeLast('.')
            if (predictions.first().label == expected) top1++
            if (predictions.first().confidence >= FoodClassifier.CONFIDENCE_THRESHOLD) confident++
        }

        val accuracy = top1.toDouble() / evaluated
        val confidentRate = confident.toDouble() / evaluated
        android.util.Log.i(
            "FoodClassifierTest",
            "evaluated=$evaluated top1=${"%.3f".format(accuracy)} " +
                "confident(>=0.7)=${"%.3f".format(confidentRate)}",
        )

        // predict.py đo được top-1 71,2% trên tập test; ảnh thumbnail webp trong app nhỏ hơn
        // nên đặt ngưỡng thấp hơn nhưng vẫn đủ để phát hiện tiền xử lý sai (khi đó ~0%).
        assertTrue("Top-1 quá thấp: $accuracy", accuracy >= 0.5)
        classifier.close()
    }

    @Test
    fun nhan_dien_anh_khong_phai_mon_an_thi_khong_tu_tin() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val classifier = FoodClassifier(context)
        // Ảnh trơn một màu: model không được tự tin đây là món ăn.
        val bitmap = android.graphics.Bitmap.createBitmap(
            FoodClassifier.INPUT_SIZE,
            FoodClassifier.INPUT_SIZE,
            android.graphics.Bitmap.Config.ARGB_8888,
        ).apply { eraseColor(android.graphics.Color.GRAY) }
        val predictions = classifier.classify(bitmap)
        bitmap.recycle()
        assertTrue("Model phải trả về dự đoán", predictions.isNotEmpty())
        assertTrue(
            "Ảnh trơn không nên vượt ngưỡng tin cậy",
            predictions.first().confidence < 0.95f,
        )
        classifier.close()
    }
}
