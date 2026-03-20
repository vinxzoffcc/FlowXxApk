package com.vinz.flowx.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.roundToInt

data class Stats(
    val cpu: Int,           // CPU load %
    val cpuFreq: String,    // CPU freq dalam GHz misal "2.30"
    val ram: Int,           // RAM usage %
    val temp: Float,        // Suhu baterai °C
    val battery: Int        // Baterai %
)

object SystemStats {

    fun getStats(context: Context): Stats {
        return Stats(
            cpu = getCpuLoad(),
            cpuFreq = getCpuFreq(),
            ram = getRamUsage(context),
            temp = getBatteryTemp(context),
            battery = getBatteryLevel(context)
        )
    }

    /**
     * Baca CPU frequency dari /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq
     * Return dalam format "2.30" GHz
     */
    private fun getCpuFreq(): String {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                val khz = freqFile.readText().trim().toLongOrNull() ?: 0L
                val ghz = khz / 1_000_000.0
                String.format("%.2f", ghz)
            } else "N/A"
        } catch (e: Exception) { "N/A" }
    }

    /**
     * Estimasi CPU load dari /proc/stat
     */
    private fun getCpuLoad(): Int {
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine() ?: return 0
            reader.close()
            val parts = line.split("\\s+".toRegex()).drop(1)
            val user   = parts[0].toLong()
            val nice   = parts[1].toLong()
            val system = parts[2].toLong()
            val idle   = parts[3].toLong()
            val iowait = parts[4].toLong()
            val total = user + nice + system + idle + iowait
            val active = user + nice + system
            if (total == 0L) 0 else ((active * 100f / total).roundToInt()).coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    /**
     * RAM usage % dari ActivityManager
     */
    private fun getRamUsage(context: Context): Int {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val used = mi.totalMem - mi.availMem
            ((used * 100f / mi.totalMem).roundToInt()).coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    /**
     * Suhu baterai dari BatteryManager (dalam °C)
     */
    private fun getBatteryTemp(context: Context): Float {
        return try {
            val intent = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            temp / 10f // BatteryManager kasih dalam 0.1°C
        } catch (e: Exception) { 0f }
    }

    /**
     * Level baterai %
     */
    private fun getBatteryLevel(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) { 0 }
    }
}
