package com.vinz.flowx.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.vinz.flowx.MainActivity
import com.vinz.flowx.R
import com.vinz.flowx.utils.ShizukuHelper
import com.vinz.flowx.utils.SystemStats
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    // View refs
    private lateinit var tvCpu: TextView
    private lateinit var tvRam: TextView
    private lateinit var tvBat: TextView
    private lateinit var tvTmp: TextView
    private lateinit var tvFps: TextView

    // HUD options (bisa diubah dari fragment)
    companion object {
        var showCpu = true
        var showRam = true
        var showBat = true
        var showTmp = true
        var showFps = true
        var isActive = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
        startForegroundNotification()
        isActive = true
        startUpdating()
    }

    private fun createOverlay() {
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_hud, null)

        tvCpu = overlayView.findViewById(R.id.tv_cpu)
        tvRam = overlayView.findViewById(R.id.tv_ram)
        tvBat = overlayView.findViewById(R.id.tv_bat)
        tvTmp = overlayView.findViewById(R.id.tv_tmp)
        tvFps = overlayView.findViewById(R.id.tv_fps)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 60
        }

        windowManager.addView(overlayView, params)

        // Drag to move
        var initX = 0; var initY = 0; var initTX = 0; var initTY = 0
        overlayView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = params.x; initY = params.y
                    initTX = event.rawX.toInt(); initTY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initX + (event.rawX - initTX).toInt()
                    params.y = initY + (event.rawY - initTY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                }
            }
            true
        }
    }

    private fun startUpdating() {
        isRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                updateStats()
                handler.postDelayed(this, 1500) // update tiap 1.5 detik
            }
        })
    }

    private fun updateStats() {
        val stats = SystemStats.getStats(this)

        if (showCpu) {
            tvCpu.text = "CPU ${stats.cpuFreq} GHz"
            tvCpu.visibility = View.VISIBLE
        } else tvCpu.visibility = View.GONE

        if (showRam) {
            tvRam.text = "RAM ${stats.ram}%"
            tvRam.visibility = View.VISIBLE
        } else tvRam.visibility = View.GONE

        if (showBat) {
            tvBat.text = "${stats.battery}%"
            tvBat.visibility = View.VISIBLE
        } else tvBat.visibility = View.GONE

        if (showTmp) {
            tvTmp.text = "${stats.temp}°C"
            tvTmp.visibility = View.VISIBLE
        } else tvTmp.visibility = View.GONE

        if (showFps) {
            // FPS estimasi dari CPU load
            val fps = if (stats.cpu > 80) "30-45" else if (stats.cpu > 50) "55-75" else "90+"
            tvFps.text = "$fps FPS"
            tvFps.visibility = View.VISIBLE
        } else tvFps.visibility = View.GONE
    }

    private fun startForegroundNotification() {
        val channelId = "flowx_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "FlowX HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Overlay HUD aktif di atas game" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notifIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notifIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("FlowX HUD Aktif")
            .setContentText("Overlay monitor berjalan di atas game")
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        isActive = false
        handler.removeCallbacksAndMessages(null)
        if (::overlayView.isInitialized) {
            try { windowManager.removeView(overlayView) } catch (e: Exception) {}
        }
    }
}
