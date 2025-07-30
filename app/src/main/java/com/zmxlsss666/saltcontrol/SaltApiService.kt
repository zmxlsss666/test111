package com.zmxlsss666.saltcontrol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SettingsStore {
    var apiUrl: String = "http://localhost:35373"
}

class SaltApiService(private val settings: SettingsStore) {

    data class NowPlaying(
        val title: String,
        val artist: String,
        val album: String,
        val isPlaying: Boolean,
        val position: Long,
        val volume: Float
    )

    private suspend fun apiGet(endpoint: String): JSONObject = withContext(Dispatchers.IO) {
        val url = URL("${settings.apiUrl}$endpoint")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3000
            readTimeout = 3000
        }
        val code = conn.responseCode
        val text = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(text)
        if (json.optString("status") != "success") throw Exception(json.optString("message"))
        json
    }

    suspend fun getNowPlaying(): NowPlaying {
        val json = apiGet("/api/now-playing")
        return NowPlaying(
            title = json.optString("title"),
            artist = json.optString("artist"),
            album = json.optString("album"),
            isPlaying = json.optBoolean("isPlaying"),
            position = json.optLong("position"),
            volume = json.optDouble("volume").toFloat()
        )
    }

    suspend fun playPause() = apiGet("/api/play-pause")
    suspend fun nextTrack() = apiGet("/api/next-track")
    suspend fun previousTrack() = apiGet("/api/previous-track")
    suspend fun volumeUp() = apiGet("/api/volume/up")
    suspend fun volumeDown() = apiGet("/api/volume/down")
    suspend fun mute() = apiGet("/api/mute")
}