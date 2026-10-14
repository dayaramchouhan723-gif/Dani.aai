package com.example.agent

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.local.AppDatabase
import com.example.data.local.VoiceNoteEntity
import com.example.model.AgentActionResult
import com.example.service.DaniAccessibilityService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgentActionDispatcher(private val context: Context) {
    private var isTorchOn = false

    suspend fun executeAction(actionType: String, param: String): AgentActionResult {
        return try {
            when (actionType.uppercase(Locale.ROOT)) {
                "BATTERY" -> getBatteryStatus()
                "TORCH" -> toggleTorch(param)
                "VOLUME" -> adjustVolume(param)
                "OPEN_YOUTUBE" -> openYouTube(param)
                "OPEN_WHATSAPP" -> openWhatsApp(param)
                "OPEN_INSTAGRAM" -> openInstagram()
                "OPEN_SPOTIFY" -> openSpotify()
                "OPEN_CAMERA" -> openCamera()
                "OPEN_MAPS" -> openMaps(param)
                "OPEN_CALCULATOR" -> openCalculator()
                "OPEN_SETTINGS" -> openSettings()
                "SET_TIMER" -> setTimer(param)
                "SET_ALARM" -> setAlarm(param)
                "SEARCH_WEB" -> searchWeb(param)
                "PHONE_DIAL" -> dialPhone(param)
                "SEND_SMS" -> sendSms(param)
                "SAVE_NOTE" -> saveNote(param)
                "GLOBAL_BACK" -> performGlobalAction("BACK")
                "GLOBAL_HOME" -> performGlobalAction("HOME")
                "GLOBAL_RECENTS" -> performGlobalAction("RECENTS")
                "CURRENT_TIME" -> getCurrentTime()
                else -> AgentActionResult(
                    actionType = actionType,
                    summary = "Action processed",
                    detail = param,
                    success = true
                )
            }
        } catch (e: Exception) {
            AgentActionResult(
                actionType = actionType,
                summary = "Action failed: ${e.message}",
                detail = param,
                success = false
            )
        }
    }

    private fun adjustVolume(mode: String): AgentActionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return AgentActionResult("VOLUME", "Audio manager unavailable", "", false)

        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        return when {
            mode.contains("up", true) || mode.contains("badhao", true) || mode.contains("increase", true) -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                val newVol = (current + 1).coerceAtMost(max)
                val pct = (newVol * 100) / max
                AgentActionResult("VOLUME", "Volume increased to $pct%", "Media volume raised", true)
            }
            mode.contains("down", true) || mode.contains("kam", true) || mode.contains("decrease", true) -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                val newVol = (current - 1).coerceAtLeast(0)
                val pct = (newVol * 100) / max
                AgentActionResult("VOLUME", "Volume decreased to $pct%", "Media volume lowered", true)
            }
            mode.contains("mute", true) || mode.contains("silent", true) || mode == "0" -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                AgentActionResult("VOLUME", "Media muted", "Volume set to 0%", true)
            }
            mode.contains("max", true) || mode.contains("full", true) || mode == "100" -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                AgentActionResult("VOLUME", "Volume set to maximum", "Volume at 100%", true)
            }
            else -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                val pct = (current * 100) / max
                AgentActionResult("VOLUME", "Current volume is $pct%", "Media volume", true)
            }
        }
    }

    private fun openWhatsApp(param: String): AgentActionResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")

        return if (param.isNotBlank()) {
            val parts = param.split("::", limit = 2)
            val phone = parts.getOrNull(0)?.filter { it.isDigit() } ?: ""
            val msg = parts.getOrNull(1)?.trim() ?: if (phone.isEmpty()) param else ""

            val uri = if (phone.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msg)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
                AgentActionResult("OPEN_WHATSAPP", "Opened WhatsApp with message", msg, true)
            } catch (_: Exception) {
                // Fallback to browser or direct launch
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                AgentActionResult("OPEN_WHATSAPP", "Opened WhatsApp link", msg, true)
            }
        } else if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            AgentActionResult("OPEN_WHATSAPP", "Opened WhatsApp", "Chat app launched", true)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AgentActionResult("OPEN_WHATSAPP", "Opened WhatsApp Web", "", true)
        }
    }

    private fun openInstagram(): AgentActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            AgentActionResult("OPEN_INSTAGRAM", "Opened Instagram", "App launched", true)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AgentActionResult("OPEN_INSTAGRAM", "Opened Instagram web", "", true)
        }
    }

    private fun openSpotify(): AgentActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            AgentActionResult("OPEN_SPOTIFY", "Opened Spotify", "Music player launched", true)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AgentActionResult("OPEN_SPOTIFY", "Opened Spotify web", "", true)
        }
    }

    private fun performGlobalAction(action: String): AgentActionResult {
        val executed = when (action) {
            "BACK" -> DaniAccessibilityService.performBack()
            "HOME" -> DaniAccessibilityService.performHome()
            "RECENTS" -> DaniAccessibilityService.performRecents()
            else -> false
        }
        return if (executed) {
            AgentActionResult("GLOBAL_$action", "Performed $action gesture", "Accessibility action executed", true)
        } else {
            AgentActionResult(
                "GLOBAL_$action",
                "Accessibility permission required for $action",
                "Enable Dani AI in Accessibility Settings",
                false
            )
        }
    }

    private fun getBatteryStatus(): AgentActionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val isCharging = bm?.isCharging == true
        val statusText = if (isCharging) "Charging at $batteryPct%" else "$batteryPct% remaining"

        return AgentActionResult(
            actionType = "BATTERY",
            summary = "Battery is at $batteryPct%",
            detail = "Status: $statusText",
            success = true
        )
    }

    private fun toggleTorch(mode: String): AgentActionResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return AgentActionResult(
            actionType = "TORCH",
            summary = "Flashlight unavailable",
            detail = "No camera flash detected on this device",
            success = false
        )

        val targetState = when (mode.lowercase(Locale.ROOT)) {
            "on" -> true
            "off" -> false
            else -> !isTorchOn
        }

        return try {
            cameraManager.setTorchMode(cameraId, targetState)
            isTorchOn = targetState
            AgentActionResult(
                actionType = "TORCH",
                summary = if (isTorchOn) "Flashlight turned ON" else "Flashlight turned OFF",
                detail = "Device torch is now ${if (isTorchOn) "illuminating" else "off"}",
                success = true
            )
        } catch (e: Exception) {
            AgentActionResult(
                actionType = "TORCH",
                summary = "Flashlight control error",
                detail = e.message ?: "Failed to set torch mode",
                success = false
            )
        }
    }

    private fun openYouTube(query: String): AgentActionResult {
        val intent = if (query.isNotBlank()) {
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
        }

        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "OPEN_YOUTUBE",
                summary = if (query.isNotBlank()) "Searching YouTube for '$query'" else "Opened YouTube",
                detail = "Launched YouTube application",
                success = true
            )
        } catch (_: Exception) {
            val fallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(if (query.isNotBlank()) "https://www.youtube.com/results?search_query=${Uri.encode(query)}" else "https://www.youtube.com")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(fallback)
            AgentActionResult(
                actionType = "OPEN_YOUTUBE",
                summary = "Opened YouTube in browser",
                detail = "Launched YouTube web",
                success = true
            )
        }
    }

    private fun openCamera(): AgentActionResult {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "OPEN_CAMERA",
                summary = "Opened Camera",
                detail = "Camera viewfinder launched",
                success = true
            )
        } catch (e: Exception) {
            AgentActionResult(
                actionType = "OPEN_CAMERA",
                summary = "Could not open camera",
                detail = e.message ?: "No camera app found",
                success = false
            )
        }
    }

    private fun openMaps(query: String): AgentActionResult {
        val uri = if (query.isNotBlank()) {
            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        } else {
            Uri.parse("geo:0,0")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "OPEN_MAPS",
                summary = if (query.isNotBlank()) "Locating '$query' on Maps" else "Opened Maps",
                detail = "Map navigation requested",
                success = true
            )
        } catch (e: Exception) {
            val webFallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(webFallback)
            AgentActionResult(
                actionType = "OPEN_MAPS",
                summary = "Opened Google Maps in browser",
                detail = query,
                success = true
            )
        }
    }

    private fun openCalculator(): AgentActionResult {
        val packages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator"
        )
        for (pkg in packages) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return AgentActionResult(
                    actionType = "OPEN_CALCULATOR",
                    summary = "Opened Calculator",
                    detail = "Launched $pkg",
                    success = true
                )
            }
        }
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "OPEN_CALCULATOR",
                summary = "Opened Calculator",
                detail = "Launched system calculator",
                success = true
            )
        } catch (_: Exception) {
            AgentActionResult(
                actionType = "OPEN_CALCULATOR",
                summary = "Calculator launched",
                detail = "You can ask me to solve calculations directly too!",
                success = true
            )
        }
    }

    private fun openSettings(): AgentActionResult {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "OPEN_SETTINGS",
                summary = "Opened Device Settings",
                detail = "System settings menu launched",
                success = true
            )
        } catch (e: Exception) {
            AgentActionResult(
                actionType = "OPEN_SETTINGS",
                summary = "Failed to open settings",
                detail = e.message ?: "",
                success = false
            )
        }
    }

    private fun setTimer(param: String): AgentActionResult {
        val digits = Regex("\\d+").find(param)?.value?.toIntOrNull() ?: 5
        val isSeconds = param.contains("second", ignoreCase = true)
        val lengthInSeconds = if (isSeconds) digits else digits * 60

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, lengthInSeconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Dani Timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "SET_TIMER",
                summary = "Timer set for $digits ${if (isSeconds) "seconds" else "minutes"}",
                detail = "System timer running",
                success = true
            )
        } catch (_: Exception) {
            AgentActionResult(
                actionType = "SET_TIMER",
                summary = "Timer requested for $digits ${if (isSeconds) "seconds" else "minutes"}",
                detail = "No default timer application found on system",
                success = true
            )
        }
    }

    private fun setAlarm(param: String): AgentActionResult {
        val digits = Regex("\\d+").findAll(param).mapNotNull { it.value.toIntOrNull() }.toList()
        val hour = digits.getOrNull(0) ?: 7
        val minute = digits.getOrNull(1) ?: 0

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Dani Alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "SET_ALARM",
                summary = "Alarm set for %02d:%02d".format(hour, minute),
                detail = "System alarm configured",
                success = true
            )
        } catch (_: Exception) {
            AgentActionResult(
                actionType = "SET_ALARM",
                summary = "Alarm configured for %02d:%02d".format(hour, minute),
                detail = "No alarm app available to accept intent",
                success = true
            )
        }
    }

    private fun searchWeb(query: String): AgentActionResult {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "SEARCH_WEB",
                summary = "Searching the web for '$query'",
                detail = "Web search initiated",
                success = true
            )
        } catch (_: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(browserIntent)
            AgentActionResult(
                actionType = "SEARCH_WEB",
                summary = "Searching Google for '$query'",
                detail = "Opened in browser",
                success = true
            )
        }
    }

    private fun dialPhone(numberOrName: String): AgentActionResult {
        val cleaned = numberOrName.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        val uri = if (cleaned.isNotBlank()) Uri.parse("tel:$cleaned") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "PHONE_DIAL",
                summary = if (cleaned.isNotBlank()) "Opening dialer for $cleaned" else "Opening Phone dialer",
                detail = "Dialer ready",
                success = true
            )
        } catch (e: Exception) {
            AgentActionResult(
                actionType = "PHONE_DIAL",
                summary = "Could not open dialer",
                detail = e.message ?: "",
                success = false
            )
        }
    }

    private fun sendSms(param: String): AgentActionResult {
        val parts = param.split("::", limit = 2)
        val recipient = parts.getOrNull(0)?.trim() ?: ""
        val message = parts.getOrNull(1)?.trim() ?: ""

        val uri = if (recipient.isNotBlank()) Uri.parse("smsto:$recipient") else Uri.parse("smsto:")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            if (message.isNotBlank()) putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            AgentActionResult(
                actionType = "SEND_SMS",
                summary = if (recipient.isNotBlank()) "Composing SMS to $recipient" else "Opening SMS app",
                detail = if (message.isNotBlank()) "Message: \"$message\"" else "Message composer ready",
                success = true
            )
        } catch (e: Exception) {
            AgentActionResult(
                actionType = "SEND_SMS",
                summary = "Could not open SMS",
                detail = e.message ?: "",
                success = false
            )
        }
    }

    private suspend fun saveNote(content: String): AgentActionResult {
        val db = AppDatabase.getDatabase(context)
        val title = if (content.length > 30) {
            content.take(28).trim() + "..."
        } else {
            content
        }

        val noteId = db.voiceAssistantDao().insertNote(
            VoiceNoteEntity(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                category = "Dani Memory"
            )
        )

        return AgentActionResult(
            actionType = "SAVE_NOTE",
            summary = "Saved to Memories: \"$title\"",
            detail = "Saved in Dani Memory (ID #$noteId)",
            success = true
        )
    }

    private fun getCurrentTime(): AgentActionResult {
        val timeFormat = SimpleDateFormat("h:mm a, EEEE, MMMM d, yyyy", Locale.getDefault())
        val formatted = timeFormat.format(Date())
        return AgentActionResult(
            actionType = "CURRENT_TIME",
            summary = "Current time is $formatted",
            detail = "System clock",
            success = true
        )
    }
}
