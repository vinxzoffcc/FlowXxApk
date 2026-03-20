package com.vinz.flowx.utils

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    private const val REQUEST_CODE = 100

    /**
     * Cek apakah Shizuku aktif dan kita punya izin
     */
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && hasPermission()
        } catch (e: Exception) {
            false
        }
    }

    private fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) { false }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Request permission failed: ${e.message}")
        }
    }

    /**
     * Jalankan command adb shell via Shizuku
     * Return: Pair(success, output)
     */
    fun run(command: String): Pair<Boolean, String> {
        return try {
            val process: Process = Shizuku.newProcess(
                arrayOf("sh", "-c", command), null, null
            )
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()
            Pair(exitCode == 0, output.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Command failed '$command': ${e.message}")
            Pair(false, e.message ?: "Error")
        }
    }

    /**
     * Jalankan banyak command sekaligus
     */
    fun runAll(commands: List<String>): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        for (cmd in commands) {
            val (ok, _) = run(cmd)
            results[cmd] = ok
        }
        return results
    }

    // ── MODULE COMMANDS ────────────────────────────────────────────

    val CMD_FPS_BOOST = listOf(
        "settings put system peak_refresh_rate 120",
        "settings put system min_refresh_rate 60",
        "settings put global gpu_debug_layers ''",
        "settings put global window_animation_scale 0.5",
        "settings put global transition_animation_scale 0.5",
        "settings put global animator_duration_scale 0.5",
        "settings put global hardware_renderer_disabled 0"
    )

    val CMD_THERMAL = listOf(
        "settings put global background_process_limit 4",
        "settings put global auto_sync_enabled 0",
        "settings put global adaptive_battery_management_enabled 0",
        "svc nfc disable"
    )

    val CMD_NETWORK = listOf(
        "settings put global wifi_sleep_policy 2",
        "settings put global wifi_scan_always_enabled 0",
        "settings put global private_dns_mode hostname",
        "settings put global private_dns_specifier 1dot1dot1dot1.cloudflare-dns.com",
        "settings put global dataSaver 0"
    )

    val CMD_RAM = listOf(
        "settings put global cached_apps_freezer enabled",
        "settings put global zram_enabled 1",
        "settings put global app_standby_enabled 1"
    )

    val CMD_GRAPHICS = listOf(
        "settings put global enable_gpu_debug_layers 0",
        "settings put system pointer_speed 4",
        "settings put global policy_control immersive.full=*"
    )

    val CMD_GAME = listOf(
        "settings put global low_power 0",
        "settings put global low_power_sticky 0",
        "settings put secure navigation_mode 2",
        "settings put system gaming_mode_enabled 1"
    )

    val CMD_RESTORE = listOf(
        "settings put global window_animation_scale 1.0",
        "settings put global transition_animation_scale 1.0",
        "settings put global animator_duration_scale 1.0",
        "settings put global auto_sync_enabled 1",
        "settings put global wifi_sleep_policy 0",
        "settings put global adaptive_battery_management_enabled 1",
        "settings put global private_dns_mode off",
        "settings put global background_process_limit 0",
        "svc nfc enable",
        "settings put global policy_control null*"
    )
}
