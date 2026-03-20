package com.vinz.flowx

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vinz.flowx.service.OverlayService
import com.vinz.flowx.ui.BoostFragment
import com.vinz.flowx.ui.HudFragment
import com.vinz.flowx.ui.AiFragment
import com.vinz.flowx.ui.SettingsFragment
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Izin overlay aktif!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNav()
        checkPermissions()

        // Default tab
        if (savedInstanceState == null) {
            loadFragment(BoostFragment())
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_boost -> { loadFragment(BoostFragment()); true }
                R.id.nav_hud   -> { loadFragment(HudFragment()); true }
                R.id.nav_ai    -> { loadFragment(AiFragment()); true }
                R.id.nav_settings -> { loadFragment(SettingsFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkPermissions() {
        // Minta izin Draw Over Other Apps
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        // Cek Shizuku
        try {
            if (Shizuku.pingBinder()) {
                // Shizuku aktif
            }
        } catch (e: Exception) {
            // Shizuku tidak tersedia
        }
    }

    fun startOverlayService() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Aktifkan izin Draw Over Apps dulu", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
    }
}
