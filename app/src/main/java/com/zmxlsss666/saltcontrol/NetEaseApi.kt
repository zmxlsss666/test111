package com.zmxlsss666.saltcontrol

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class NetEaseApi {
    data class SongInfo(val title: String, val artist: String)

    suspend fun getLyric(info: SongInfo): String = withContext(Dispatchers.IO) {
        // 占位网易云歌词API
        val url = URL("https://netease-lyric-api.example.com/lyric?title=${info.title}&artist=${info.artist}")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3000
            readTimeout = 3000
        }
        val text = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(text)
        json.optString("lyric", "")
    }

    suspend fun getCover(info: SongInfo): Bitmap? = withContext(Dispatchers.IO) {
        // 占位网易云封面API
        val url = URL("https://netease-cover-api.example.com/cover?title=${info.title}&artist=${info.artist}")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3000
            readTimeout = 3000
        }
        val bytes = conn.inputStream.readBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}