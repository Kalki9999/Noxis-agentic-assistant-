package com.example.noxis.brain

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
import java.util.concurrent.TimeUnit

class GeminiApiClient {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val model = "gemini-3.5-flash"
  private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

  fun isKeyConfigured(): Boolean {
    val key = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }
    return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
  }

  suspend fun generateJarvisResponse(
    userPrompt: String,
    systemInstructionText: String,
    conversationHistory: List<Pair<String, String>> = emptyList()
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }

    if (!isKeyConfigured()) {
      return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY not configured in Secrets panel"))
    }

    try {
      val rootJson = JSONObject()

      // System instruction
      val sysInstructionJson = JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", systemInstructionText) })
        })
      }
      rootJson.put("systemInstruction", sysInstructionJson)

      // Contents array
      val contentsArray = JSONArray()

      // Include recent conversation context (last 6 messages)
      val recentTurns = conversationHistory.takeLast(6)
      for ((role, text) in recentTurns) {
        val turnRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
        contentsArray.put(JSONObject().apply {
          put("role", turnRole)
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", text) })
          })
        })
      }

      // Add current user prompt
      contentsArray.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", userPrompt) })
        })
      })

      rootJson.put("contents", contentsArray)

      // Generation config
      val genConfig = JSONObject().apply {
        put("temperature", 0.7)
        put("topP", 0.95)
      }
      rootJson.put("generationConfig", genConfig)

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val requestBody = rootJson.toString().toRequestBody(mediaType)

      val url = "$baseUrl?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) {
          Log.e("GeminiApiClient", "API call failed code ${response.code}: $responseBody")
          return@withContext Result.failure(Exception("Gemini API error code ${response.code}"))
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
          val candidate = candidates.getJSONObject(0)
          val content = candidate.optJSONObject("content")
          val parts = content?.optJSONArray("parts")
          if (parts != null && parts.length() > 0) {
            val text = parts.getJSONObject(0).optString("text", "")
            return@withContext Result.success(text)
          }
        }
        Result.failure(Exception("No content returned from Gemini model"))
      }
    } catch (e: Exception) {
      Log.e("GeminiApiClient", "Call exception", e)
      Result.failure(e)
    }
  }
}
