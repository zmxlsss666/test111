package com.zmxlsss666.saltcontrol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

object LanScanner {
    // 简单扫描局域网内35373端口的设备
    suspend fun scanLan(): List<String> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<String>()
        val subnet = getLocalSubnet() ?: return@withContext devices
        val jobs = (1..254).map { i ->
            kotlinx.coroutines.async {
                val ip = "$subnet.$i"
                try {
                    Socket(ip, 35373).use {
                        devices.add("http://$ip:35373")
                    }
                } catch (_: Exception) {}
            }
        }
        jobs.forEach { it.await() }
        devices
    }

    private fun getLocalSubnet(): String? {
        val addrs = InetAddress.getAllByName(InetAddress.getLocalHost().hostAddress)
        for (addr in addrs) {
            val ip = addr.hostAddress
            val parts = ip.split(".")
            if (parts.size == 4) return "${parts[0]}.${parts[1]}.${parts[2]}"
        }
        return null
    }
}