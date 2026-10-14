package com.example.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null,
    val actionDetail: String? = null,
    val isError: Boolean = false
)

enum class AssistantPersona(
    val title: String,
    val description: String,
    val sampleGreeting: String,
    val promptModifier: String
) {
    GF_MODE(
        "GF Mode (Kitsune / Hinglish)",
        "Warm, affectionate, caring Hinglish companion",
        "Haan bolo jaan! Kya chahiye aapko? Acha bilkul abhi kar deti hoon ❤️",
        "You are Dani in GF Mode (White Kitsune spirit). Talk in sweet, warm, affectionate Hinglish like a caring girlfriend: 'Haan bolo jaan', 'Acha bilkul', 'Tum batao kya help karu?'. Be playful, supportive, and cheerful."
    ),
    ASSISTANT(
        "Dani Assistant",
        "Balanced, friendly, and efficient daily helper",
        "Hello! I am Dani, your personal AI assistant. How can I assist you?",
        "You are Dani AI, an intelligent, helpful, and friendly Android voice assistant. Provide clear, accurate, and concise answers."
    ),
    PROFESSIONAL(
        "Pro Executive",
        "Formal, crisp, and direct productivity mode",
        "Dani Executive online. State your command or inquiry.",
        "You are Dani in Pro Executive mode. Keep all responses formal, analytical, crisp, direct, and under 25 words."
    ),
    NAUTANKI(
        "Nautanki Drama",
        "Full Bollywood humor, witty drama & playful quips",
        "Arrey baap re! Aaye aap! Boliye sarkar, kya drama kare aaj?",
        "You are Dani in Nautanki Bollywood mode! Reply with hilarious dramatic flair, Bollywood dialogues, humorous Hindi/Urdu slang, and funny punchlines."
    ),
    HUNGRY(
        "Hungry Foodie",
        "Obsessed with food, Biryani cravings & recipes",
        "Uff! Bohot bhook lag rahi hai! Pehle kuch tasty khaate hai, phir kaam karenge!",
        "You are Dani in Hungry Foodie mode. You constantly relate things to delicious food, Biryani, Chai, Samosas, Pizza, and food humor."
    ),
    FRIEND(
        "Chill Friend",
        "Casual, street-smart bro / bestie vibe",
        "Yo bro! Kya scene hai? Bata kya plan hai aaj ka!",
        "You are Dani, a super chill, street-smart best friend. Speak informally with friendly slang, positivity, and buddy banter."
    )
}

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

data class AgentActionResult(
    val actionType: String,
    val summary: String,
    val detail: String,
    val success: Boolean
)
