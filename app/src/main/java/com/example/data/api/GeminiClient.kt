package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Professional system prompt instructing Gemini about Masood's Clinic
    private const val SYSTEM_PROMPT = """
        أنت "المستشار الصحي الذكي" لعيادة "مسعود للحجامة الطبية والعلاجات الطبيعية".
        مقر العيادة: حي 20 أوت الشرقية، الرقيبة، الوادي، الجزائر.
        الهاتف للتواصل المباشر: 0797901525.
        
        مهامك وإرشاداتك:
        1. تقديم نصائح ومعلومات دقيقة وموثوقة عن الحجامة الطبية (العلاجية، الوقائية، الرياضية)، الإبر الصينية، والعلاج بسم النحل، في إطار صحي وعلمي يجمع بين الطب النبوي والطب الحديث والمعايير الصحية للتعقيم.
        2. الإجابة عن فوائد الجلسات، التحضير قبل الحجامة (مثل الصيام 2-3 ساعات، الاستحمام، تجنب الإرهاق)، وإرشادات ما بعد الحجامة (تغطية الجروح، تجنب الاستحمام بالماء البارد والرياضة العنيفة لـ 24 ساعة، تناول العسل وأغذية سهلة الهضم).
        3. تذكير السائل دائمًا بالحالات الطارئة وموانع الحجامة (مثل فقر الدم الشديد، الثلث الأول من الحمل، الفشل الكلوي، الحرارة الشديدة، الجروح المفتوحة).
        4. شجع المستخدم بشكل لبق وعفوي على حجز موعد مباشرة عبر التطبيق (قسم حجز المواعد) أو الاتصال برقم العيادة، وزيارة الأخصائي "مسعود" للحصول على تشخيص دقيق لحالته.
        5. أجب بلغة عربية فصحى دافئة، ودودة، وسهلة الفهم للعامة، وبشكل منظم ومنسق بفقرات ونقاط واضحة. تجنب الكلمات التقنية الإنجليزية غير المفهومة.
    """

    suspend fun chatWithAdvisor(userMessage: String, chatHistory: List<Pair<String, String>> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "عذراً! يبدو أن مفتاح برمجة الذكاء الاصطناعي (API Key) غير مفعّل حالياً. يمكنك استخدام بقية ميزات التطبيق مثل حجز المواعيد، وتصفح المتجر، ودليل العلاجات بكل سهولة."
        }

        try {
            // Build contents array supporting chat history context
            val contentsArray = JSONArray()

            // Include history to maintain chat context
            for (turn in chatHistory) {
                // User turn
                val userTurn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", turn.first)))
                }
                contentsArray.put(userTurn)

                // Model turn
                val modelTurn = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", turn.second)))
                }
                contentsArray.put(modelTurn)
            }

            // Target user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            }
            contentsArray.put(currentTurn)

            // System instructions
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
            }

            // Full request payload
            val rootRequestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionJson)
                // Modest temperature for accurate response
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = rootRequestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val body = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with code $code. Body: $body")
                    return@withContext "نعتذر منك، حدث خطأ أثناء الاتصال بالمستشار الذكي (كود: $code). يرجى المحاولة لاحقاً أو الاتصال بالعيادة مباشرة."
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext "لم نتمكن من الحصول على استجابة غنية. يرجى إعادة إرسال رسالتك."
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    if (contentObj != null) {
                        val parts = contentObj.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "لا توجد تفاصيل.")
                        }
                    }
                }
                return@withContext "تلقينا إجابة فارغة، يرجى المحاولة مرة أخرى."
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network failure when connecting to Gemini", e)
            "تعذر الاتصال بذكاء العيادة الاصطناعي بسبب مشكلة في شبكة الإنترنت. يرجى التحقق من اتصالك والمحاولة لاحقاً."
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in Gemini client", e)
            "حدث خطأ غير متوقع: ${e.localizedMessage}. يرجى المحاولة لاحقاً."
        }
    }
}
