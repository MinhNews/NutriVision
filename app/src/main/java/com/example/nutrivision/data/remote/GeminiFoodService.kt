package com.example.nutrivision.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.nutrivision.BuildConfig
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Kết quả Gemini trả về cho một bức ảnh món ăn. */
data class AiDish(
    val name: String,
    val isDrink: Boolean,
    val portionGrams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val confidence: Double,
    val note: String?,
)

sealed interface AiDishResult {
    data class Success(val dish: AiDish) : AiDishResult
    data class Failure(val message: String) : AiDishResult
}

/**
 * Gọi Gemini (REST, không thêm SDK) khi model trên máy không chắc chắn.
 *
 * Chỉ gửi ảnh khi người dùng bấm "Nhờ AI mô tả" — đúng như đã ghi trong Điều khoản.
 * Khoá đọc từ local.properties → BuildConfig.GEMINI_API_KEY, không nằm trong mã nguồn.
 */
class GeminiFoodService(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = MODEL,
) {

    val isConfigured: Boolean get() = apiKey.isNotBlank()

    suspend fun describeMeal(bitmap: Bitmap): AiDishResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext AiDishResult.Failure(
                tr(
                    "Chưa cấu hình khoá Gemini (gemini.api.key trong local.properties).",
                    "Gemini key is not configured (gemini.api.key in local.properties).",
                )
            )
        }
        val payload = runCatching { buildRequestBody(bitmap) }.getOrElse {
            return@withContext AiDishResult.Failure(
                tr("Không đọc được ảnh để gửi AI.", "Could not read the photo to send to AI.")
            )
        }
        val response = runCatching { post(payload) }.getOrElse { error ->
            return@withContext AiDishResult.Failure(
                tr(
                    "Không gọi được Gemini: ${error.message ?: "lỗi mạng"}",
                    "Could not reach Gemini: ${error.message ?: "network error"}",
                )
            )
        }
        when (response.code) {
            HttpURLConnection.HTTP_OK -> parse(response.body)
            400, 403 -> AiDishResult.Failure(
                tr(
                    "Khoá Gemini không hợp lệ hoặc chưa bật Generative Language API.",
                    "Gemini key is invalid or the Generative Language API is not enabled.",
                )
            )
            429 -> AiDishResult.Failure(
                tr("Gemini đang giới hạn lượt gọi. Thử lại sau ít phút.", "Gemini rate limit reached. Try again shortly.")
            )
            else -> AiDishResult.Failure(
                tr(
                    "Gemini trả về lỗi ${response.code}.",
                    "Gemini returned error ${response.code}.",
                )
            )
        }
    }

    private fun buildRequestBody(bitmap: Bitmap): String {
        val jpeg = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
        val base64 = Base64.encodeToString(jpeg, Base64.NO_WRAP)

        val parts = JSONArray()
            .put(JSONObject().put("text", PROMPT))
            .put(
                JSONObject().put(
                    "inline_data",
                    JSONObject().put("mime_type", "image/jpeg").put("data", base64),
                )
            )

        return JSONObject()
            .put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.2)
                    .put("responseMimeType", "application/json")
                    .put("responseSchema", responseSchema()),
            )
            .put(
                "safetySettings",
                JSONArray()
                    .put(safety("HARM_CATEGORY_HARASSMENT"))
                    .put(safety("HARM_CATEGORY_HATE_SPEECH"))
                    .put(safety("HARM_CATEGORY_SEXUALLY_EXPLICIT"))
                    .put(safety("HARM_CATEGORY_DANGEROUS_CONTENT")),
            )
            .toString()
    }

    private fun safety(category: String) = JSONObject()
        .put("category", category)
        .put("threshold", "BLOCK_ONLY_HIGH")

    private fun responseSchema(): JSONObject {
        val properties = JSONObject()
            .put("dish", JSONObject().put("type", "STRING"))
            .put("is_drink", JSONObject().put("type", "BOOLEAN"))
            .put("portion_g", JSONObject().put("type", "NUMBER"))
            .put("calories", JSONObject().put("type", "NUMBER"))
            .put("protein_g", JSONObject().put("type", "NUMBER"))
            .put("carbs_g", JSONObject().put("type", "NUMBER"))
            .put("fat_g", JSONObject().put("type", "NUMBER"))
            .put("confidence", JSONObject().put("type", "NUMBER"))
            .put("note", JSONObject().put("type", "STRING"))
        return JSONObject()
            .put("type", "OBJECT")
            .put("properties", properties)
            .put(
                "required",
                JSONArray(listOf("dish", "is_drink", "portion_g", "calories", "confidence")),
            )
    }

    private fun post(payload: String): HttpResponse {
        val url = URL("$ENDPOINT?key=$apiKey")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        try {
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return HttpResponse(code, body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String): AiDishResult {
        val json = runCatching { JSONObject(body) }.getOrElse {
            return AiDishResult.Failure(tr("Gemini trả về dữ liệu lạ.", "Gemini returned unexpected data."))
        }
        val text = json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            .orEmpty()
        if (text.isBlank()) {
            return AiDishResult.Failure(
                tr(
                    "Gemini không mô tả được ảnh này. Chụp lại rõ hơn giúp ích.",
                    "Gemini could not describe this photo. A clearer shot helps.",
                )
            )
        }
        val dish = runCatching { JSONObject(stripFences(text)) }.getOrElse {
            return AiDishResult.Failure(tr("Gemini trả về dữ liệu lạ.", "Gemini returned unexpected data."))
        }
        val name = dish.optString("dish").trim()
        if (name.isBlank()) {
            return AiDishResult.Failure(tr("AI không nhận ra món trong ảnh.", "AI could not identify the dish."))
        }
        return AiDishResult.Success(
            AiDish(
                name = name,
                isDrink = dish.optBoolean("is_drink", false),
                portionGrams = dish.optDouble("portion_g", 100.0).coerceIn(10.0, 2000.0),
                calories = dish.optDouble("calories", 0.0).coerceAtLeast(0.0),
                protein = dish.optDouble("protein_g", 0.0).coerceAtLeast(0.0),
                carbs = dish.optDouble("carbs_g", 0.0).coerceAtLeast(0.0),
                fat = dish.optDouble("fat_g", 0.0).coerceAtLeast(0.0),
                confidence = dish.optDouble("confidence", 0.0).coerceIn(0.0, 1.0),
                note = dish.optString("note").takeIf { it.isNotBlank() },
            )
        )
    }

    private fun stripFences(text: String): String = text
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    private data class HttpResponse(val code: Int, val body: String)

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models"

        /** Model đã kiểm tra sẵn sàng với khoá của dự án. */
        const val MODEL = "gemini-2.5-flash"

        private val PROMPT = """
            Bạn là chuyên gia dinh dưỡng người Việt. Hãy nhìn ảnh và cho biết đây là món gì.
            Trả lời JSON đúng schema:
            - dish: tên món tiếng Việt ngắn gọn (ví dụ "Phở bò", "Trà sữa trân châu").
            - is_drink: true nếu là đồ uống.
            - portion_g: khối lượng ăn được ước lượng trong ảnh, đơn vị gram.
            - calories, protein_g, carbs_g, fat_g: cho ĐÚNG khẩu phần ước lượng ở trên (không phải trên 100 g).
            - confidence: 0..1, mức chắc chắn của bạn.
            - note: một câu ngắn vì sao đoán vậy.
            Nếu ảnh không có món ăn/đồ uống, đặt dish = "" và confidence = 0.
        """.trimIndent()
    }
}
