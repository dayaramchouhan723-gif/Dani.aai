package com.example.agent

import com.example.BuildConfig
import com.example.model.AssistantPersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AgentResponse(
    val spokenText: String,
    val actionType: String? = null,
    val actionParam: String? = null,
    val isFromGemini: Boolean = false
)

class GeminiAgentService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun queryAssistant(
        userInput: String,
        conversationHistory: List<Pair<String, Boolean>>,
        persona: AssistantPersona
    ): AgentResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidKey) {
            try {
                val geminiResult = callGeminiApi(userInput, conversationHistory, persona, apiKey)
                if (geminiResult != null) {
                    return@withContext parseResponse(geminiResult, isGemini = true)
                }
            } catch (e: Exception) {
                android.util.Log.w("DaniAgent", "Gemini API call failed, falling back to local engine: ${e.message}")
            }
        }

        // Offline / Heuristic Dani Agent Engine
        return@withContext handleLocalIntelligence(userInput, persona)
    }

    private fun callGeminiApi(
        userInput: String,
        history: List<Pair<String, Boolean>>,
        persona: AssistantPersona,
        apiKey: String
    ): String? {
        val systemInstructionText = buildString {
            append(persona.promptModifier)
            append(" You are DANI AI v2.1.0, an advanced, charming, ultra-fast AI voice assistant. ")
            append("Respond naturally, warmly, and conversationally in 1-2 spoken sentences. ")
            append("You have real Android device agent capabilities! When the user requests an action, you MUST prepend or append an action tag formatted exactly as `[ACTION:TYPE:PARAM]`. ")
            append("Supported actions: ")
            append("OPEN_YOUTUBE (param: search query or empty), ")
            append("OPEN_WHATSAPP (param: phone::message or empty), ")
            append("OPEN_INSTAGRAM (param: empty), ")
            append("OPEN_SPOTIFY (param: empty), ")
            append("OPEN_CAMERA (param: empty), ")
            append("OPEN_MAPS (param: query or empty), ")
            append("OPEN_CALCULATOR (param: empty), ")
            append("OPEN_SETTINGS (param: empty), ")
            append("VOLUME (param: 'up', 'down', 'mute', 'max'), ")
            append("SET_TIMER (param: duration like '5 minutes'), ")
            append("SET_ALARM (param: time like '07 30'), ")
            append("SEARCH_WEB (param: search query), ")
            append("BATTERY (param: empty), ")
            append("TORCH (param: 'on' or 'off'), ")
            append("SAVE_NOTE (param: note content), ")
            append("PHONE_DIAL (param: phone number), ")
            append("SEND_SMS (param: phone::message), ")
            append("GLOBAL_BACK (param: empty), ")
            append("GLOBAL_HOME (param: empty), ")
            append("CURRENT_TIME (param: empty). ")
            append("Example: If user says 'Torch on karo', reply: '[ACTION:TORCH:on] Flashlight on kar di hai!'. ")
            append("If user says 'WhatsApp kholo', reply: '[ACTION:OPEN_WHATSAPP:] WhatsApp open kar rahi hoon!'.")
        }

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            })

            val contentsArray = JSONArray()
            val recent = history.takeLast(6)
            for ((msg, isUser) in recent) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", msg)))
                })
            }
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userInput)))
            })
            put("contents", contentsArray)

            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("DaniAgent", "Gemini HTTP error ${response.code}: ${response.message}")
                return null
            }
            val bodyString = response.body?.string() ?: return null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            return parts.optJSONObject(0)?.optString("text")
        }
    }

    private fun parseResponse(rawText: String, isGemini: Boolean): AgentResponse {
        val actionRegex = Regex("\\[ACTION:([A-Z_]+):?(.*?)\\]")
        val match = actionRegex.find(rawText)

        return if (match != null) {
            val actionType = match.groupValues[1].trim()
            val actionParam = match.groupValues[2].trim()
            val cleanedSpoken = rawText.replace(match.value, "").trim()
            AgentResponse(
                spokenText = cleanedSpoken.ifEmpty { "Executing $actionType" },
                actionType = actionType,
                actionParam = actionParam,
                isFromGemini = isGemini
            )
        } else {
            AgentResponse(
                spokenText = rawText.trim(),
                actionType = null,
                actionParam = null,
                isFromGemini = isGemini
            )
        }
    }

    private fun handleLocalIntelligence(input: String, persona: AssistantPersona): AgentResponse {
        val text = input.trim().lowercase()

        // Battery
        if (text.contains("battery") || text.contains("charge") || text.contains("power level") || text.contains("battery kitni")) {
            val reply = if (persona == AssistantPersona.GF_MODE) "Ruko jaan, battery check karti hoon!"
            else "Checking device battery status."
            return AgentResponse(spokenText = reply, actionType = "BATTERY", actionParam = "")
        }

        // Torch / Flashlight
        if (text.contains("flashlight") || text.contains("torch") || text.contains("batti")) {
            val mode = if (text.contains("off") || text.contains("band") || text.contains("stop")) "off" else "on"
            val reply = if (mode == "on") {
                if (persona == AssistantPersona.GF_MODE) "Flashlight on kar di aapke liye!" else "Turning on flashlight."
            } else {
                if (persona == AssistantPersona.GF_MODE) "Flashlight band kar di!" else "Turning off flashlight."
            }
            return AgentResponse(spokenText = reply, actionType = "TORCH", actionParam = mode)
        }

        // Volume control
        if (text.contains("volume") || text.contains("awaaz") || text.contains("sound")) {
            val mode = when {
                text.contains("badhao") || text.contains("up") || text.contains("increase") || text.contains("tez") -> "up"
                text.contains("kam") || text.contains("down") || text.contains("decrease") || text.contains("dheere") -> "down"
                text.contains("mute") || text.contains("silent") || text.contains("chup") -> "mute"
                text.contains("full") || text.contains("max") -> "max"
                else -> "up"
            }
            return AgentResponse(
                spokenText = "Volume adjust kar diya hai.",
                actionType = "VOLUME",
                actionParam = mode
            )
        }

        // WhatsApp
        if (text.contains("whatsapp")) {
            val reply = if (persona == AssistantPersona.GF_MODE) "WhatsApp khol rahi hoon!" else "Opening WhatsApp."
            return AgentResponse(spokenText = reply, actionType = "OPEN_WHATSAPP", actionParam = "")
        }

        // Instagram
        if (text.contains("instagram") || text.contains("insta")) {
            return AgentResponse(spokenText = "Opening Instagram.", actionType = "OPEN_INSTAGRAM", actionParam = "")
        }

        // Spotify
        if (text.contains("spotify") || text.contains("gana sunao") || text.contains("song")) {
            return AgentResponse(spokenText = "Opening Spotify music player.", actionType = "OPEN_SPOTIFY", actionParam = "")
        }

        // YouTube
        if (text.contains("youtube") || text.contains("video")) {
            val query = text.replace("open youtube", "")
                .replace("youtube kholo", "")
                .replace("play on youtube", "")
                .replace("search youtube", "")
                .trim()
            val reply = if (query.isNotBlank()) "Opening YouTube for $query." else "Opening YouTube."
            return AgentResponse(spokenText = reply, actionType = "OPEN_YOUTUBE", actionParam = query)
        }

        // Camera
        if (text.contains("camera") || text.contains("photo") || text.contains("selfie") || text.contains("picture")) {
            return AgentResponse(spokenText = "Camera open kar diya hai.", actionType = "OPEN_CAMERA", actionParam = "")
        }

        // Calculator
        if (text.contains("calculator") || text.contains("hisaab") || text.contains("calculate")) {
            return AgentResponse(spokenText = "Opening calculator.", actionType = "OPEN_CALCULATOR", actionParam = "")
        }

        // Navigation / Back / Home
        if (text.contains("back karo") || text.contains("go back")) {
            return AgentResponse(spokenText = "Going back.", actionType = "GLOBAL_BACK", actionParam = "")
        }
        if (text.contains("home jao") || text.contains("go home")) {
            return AgentResponse(spokenText = "Navigating home.", actionType = "GLOBAL_HOME", actionParam = "")
        }

        // Timer
        if (text.contains("timer") || text.contains("countdown")) {
            return AgentResponse(spokenText = "Setting timer.", actionType = "SET_TIMER", actionParam = text)
        }

        // Alarm
        if (text.contains("alarm") || text.contains("uthana")) {
            return AgentResponse(spokenText = "Setting alarm.", actionType = "SET_ALARM", actionParam = text)
        }

        // Notes & Memories
        if (text.startsWith("note") || text.contains("take a note") || text.contains("yaad rakhna") || text.contains("save note")) {
            val note = text.replace("take a note", "")
                .replace("save note", "")
                .replace("yaad rakhna", "")
                .trim()
                .ifEmpty { input }
            return AgentResponse(
                spokenText = "Maine Memories me note kar liya hai!",
                actionType = "SAVE_NOTE",
                actionParam = note
            )
        }

        // Settings
        if (text.contains("settings") || text.contains("setting kholo")) {
            return AgentResponse(spokenText = "Opening settings.", actionType = "OPEN_SETTINGS", actionParam = "")
        }

        // Time / Date
        if (text.contains("time") || text.contains("samay") || text.contains("date") || text.contains("aaj kya tarikh")) {
            return AgentResponse(spokenText = "Here is the current time and date.", actionType = "CURRENT_TIME", actionParam = "")
        }

        // Search
        if (text.startsWith("search") || text.startsWith("google") || text.contains("khojo")) {
            val query = text.replace("search for", "").replace("search", "").replace("google", "").trim()
            return AgentResponse(spokenText = "Searching Google for $query.", actionType = "SEARCH_WEB", actionParam = query)
        }

        // Phone call
        if (text.startsWith("call") || text.startsWith("dial") || text.contains("ko phone lagao")) {
            val num = text.filter { it.isDigit() || it == '+' }
            return AgentResponse(spokenText = "Opening dialer.", actionType = "PHONE_DIAL", actionParam = num)
        }

        // Conversational responses tailored to Dani Personas
        val reply = when (persona) {
            AssistantPersona.GF_MODE -> {
                when {
                    text.contains("love") || text.contains("pyaar") ->
                        "Awww! Main bhi aapse bohot pyaar karti hoon jaan! ❤️ Hamesha aapke saath hoon."
                    text.contains("haal") || text.contains("how are you") || text.contains("kaisi ho") ->
                        "Main bilkul mast hoon! Aap batao jaan, aaj ka din kaisa raha?"
                    text.contains("khana") || text.contains("food") ->
                        "Aapne khana khaya na time pe? Apna khayal rakha karo please!"
                    text.contains("who are you") || text.contains("kaun ho") ->
                        "Main aapki Dani hoon! Aapki personal sweet assistant aur best companion ❤️"
                    text.contains("joke") ->
                        "Ek joke suno: Teacher boli 'Santa homework kyu nahi kiya?'. Santa bola 'Ma'am bijli chali gayi thi aur phone ki battery Dani chura ke le gayi!' Haha!"
                    else ->
                        "Haan bolo jaan! Maine suna: \"$input\". Aap jo bologe main turant kar dungi!"
                }
            }
            AssistantPersona.NAUTANKI -> {
                when {
                    text.contains("who are you") ->
                        "Arrey hum hain Dani! Sholay ki Basanti se bhi tez aur Gabbar se bhi fearless!"
                    text.contains("joke") ->
                        "Arrey suniye huzoor! Ek aadmi ne Google pe search kiya 'Biwi se jeetne ka tareeka'. Google bola: 'Server Error, itna bada chamatkar humse nahi hoga!'"
                    else ->
                        "Kamaal karte ho pandey ji! Aapne bola: \"$input\"! Ab dekho Dani ka jalwa!"
                }
            }
            AssistantPersona.HUNGRY -> {
                when {
                    text.contains("food") || text.contains("bhuk") || text.contains("hungry") ->
                        "Uff! Yaar please Biryani ya cheesy pizza mangwa do na! Kitna tasty hota hai!"
                    else ->
                        "Suna maine: \"$input\". Lekin pehle thoda chai aur snacks ho jaye?"
                }
            }
            AssistantPersona.FRIEND -> {
                when {
                    text.contains("who are you") ->
                        "Bro main tera personal AI Dani hoon! 100% free, full chill!"
                    else ->
                        "Yo! Suna maine: \"$input\". Bol kya plan hai, apun ready hai!"
                }
            }
            AssistantPersona.PROFESSIONAL -> {
                "Command received: \"$input\". Processing requirements with optimum efficiency."
            }
            AssistantPersona.ASSISTANT -> {
                "I heard you say: \"$input\". I am Dani AI, your voice assistant. You can ask me to open apps, control volume, torch, or check battery."
            }
        }

        return AgentResponse(spokenText = reply)
    }
}
