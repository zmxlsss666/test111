package com.zmxlsss666.saltcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moriafly.saltui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaltControlScreen()
        }
    }
}

@Composable
fun SaltControlScreen() {
    val scope = rememberCoroutineScope()
    val saltApi = remember { SaltApiService(SettingsStore) }
    val netEaseApi = remember { NetEaseApi() }
    var foundDevices by remember { mutableStateOf(listOf<String>()) }
    var selectedDevice by remember { mutableStateOf(SettingsStore.apiUrl) }
    var nowPlaying by remember { mutableStateOf<SaltApiService.NowPlaying?>(null) }
    var lyric by remember { mutableStateOf("") }
    var cover by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var customIp by remember { mutableStateOf(SettingsStore.apiUrl) }
    var scanning by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }

    SaltUITheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Salt Player 控制", style = MaterialTheme.typography.h4)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    scanning = true
                    scope.launch {
                        foundDevices = LanScanner.scanLan()
                        scanning = false
                    }
                }) { Text(if (scanning) "扫描中..." else "扫描局域网") }
                Spacer(modifier = Modifier.width(16.dp))
                DropdownMenu(
                    expanded = foundDevices.isNotEmpty(),
                    onDismissRequest = { foundDevices = emptyList() }
                ) {
                    foundDevices.forEach { device ->
                        DropdownMenuItem(onClick = {
                            selectedDevice = device
                            SettingsStore.apiUrl = device
                        }) { Text(device) }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "自定义API地址/IP: ")
                BasicTextField(
                    value = customIp,
                    onValueChange = { customIp = it },
                    modifier = Modifier.width(200.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (customIp.isNotBlank()) {
                        selectedDevice = customIp
                        SettingsStore.apiUrl = customIp
                    }
                }) { Text("应用") }
            }
            Divider()
            Button(onClick = {
                scope.launch {
                    try {
                        nowPlaying = saltApi.getNowPlaying()
                        apiError = null
                        nowPlaying?.let {
                            val info = NetEaseApi.SongInfo(it.title, it.artist)
                            lyric = netEaseApi.getLyric(info)
                            cover = netEaseApi.getCover(info)
                        }
                    } catch (e: Exception) {
                        apiError = "API访问失败: ${e.message}"
                    }
                }
            }) { Text("刷新当前播放") }
            apiError?.let { Text("错误: $it", color = MaterialTheme.colors.error) }
            nowPlaying?.let {
                Text("曲目: ${it.title}")
                Text("歌手: ${it.artist}")
                Text("专辑: ${it.album}")
                Text("状态: ${if (it.isPlaying) "播放中" else "暂停"}")
                Text("音量: ${it.volume}")
                Text("进度: ${it.position / 1000}s")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        saltApi.playPause()
                    }
                }) { Text("播放/暂停") }
                Button(onClick = {
                    scope.launch {
                        saltApi.nextTrack()
                    }
                }) { Text("下一曲") }
                Button(onClick = {
                    scope.launch {
                        saltApi.previousTrack()
                    }
                }) { Text("上一曲") }
                Button(onClick = {
                    scope.launch {
                        saltApi.volumeUp()
                    }
                }) { Text("音量+") }
                Button(onClick = {
                    scope.launch {
                        saltApi.volumeDown()
                    }
                }) { Text("音量-") }
                Button(onClick = {
                    scope.launch {
                        saltApi.mute()
                    }
                }) { Text("静音/取消静音") }
            }
            Divider()
            Text("歌词:")
            Text(lyric)
            cover?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "封面", modifier = Modifier.size(180.dp))
            }
        }
    }
}