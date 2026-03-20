package com.vinz.flowx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.vinz.flowx.R
import com.vinz.flowx.utils.ShizukuHelper
import com.vinz.flowx.utils.SystemStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoostFragment : Fragment() {

    private lateinit var tvRam: TextView
    private lateinit var tvCpu: TextView
    private lateinit var tvTmp: TextView
    private lateinit var tvBat: TextView
    private lateinit var progressRam: ProgressBar
    private lateinit var progressCpu: ProgressBar
    private lateinit var tvShizukuStatus: TextView
    private lateinit var btnFullBoost: Button
    private lateinit var tvBoostLog: TextView
    private lateinit var pillStatus: TextView

    private val moduleToggles = mutableMapOf<String, Switch>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_boost, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRam = view.findViewById(R.id.tv_stat_ram)
        tvCpu = view.findViewById(R.id.tv_stat_cpu)
        tvTmp = view.findViewById(R.id.tv_stat_tmp)
        tvBat = view.findViewById(R.id.tv_stat_bat)
        progressRam = view.findViewById(R.id.progress_ram)
        progressCpu = view.findViewById(R.id.progress_cpu)
        tvShizukuStatus = view.findViewById(R.id.tv_shizuku)
        btnFullBoost = view.findViewById(R.id.btn_full_boost)
        tvBoostLog = view.findViewById(R.id.tv_boost_log)
        pillStatus = view.findViewById(R.id.tv_pill)

        checkShizuku()
        startStatsUpdate()

        btnFullBoost.setOnClickListener { runFullBoost() }

        setupModuleToggles(view)
    }

    private fun checkShizuku() {
        val ok = ShizukuHelper.isAvailable()
        tvShizukuStatus.text = if (ok) "✓ Shizuku terhubung" else "✗ Shizuku belum aktif"
        tvShizukuStatus.setTextColor(
            if (ok) requireContext().getColor(R.color.green)
            else requireContext().getColor(R.color.red)
        )
        btnFullBoost.isEnabled = ok
    }

    private fun startStatsUpdate() {
        lifecycleScope.launch {
            while (isAdded) {
                val stats = withContext(Dispatchers.IO) {
                    SystemStats.getStats(requireContext())
                }
                tvRam.text = "${stats.ram}%"
                tvCpu.text = "${stats.cpu}%"
                tvTmp.text = "${stats.temp}°"
                tvBat.text = "${stats.battery}%"
                progressRam.progress = stats.ram
                progressCpu.progress = stats.cpu
                delay(2000)
            }
        }
    }

    private fun runFullBoost() {
        if (!ShizukuHelper.isAvailable()) return
        lifecycleScope.launch {
            btnFullBoost.isEnabled = false
            btnFullBoost.text = "Berjalan..."
            tvBoostLog.text = ""

            val allCmds = listOf(
                ShizukuHelper.CMD_FPS_BOOST,
                ShizukuHelper.CMD_THERMAL,
                ShizukuHelper.CMD_NETWORK,
                ShizukuHelper.CMD_RAM,
                ShizukuHelper.CMD_GRAPHICS,
                ShizukuHelper.CMD_GAME
            ).flatten()

            for (cmd in allCmds) {
                val (ok, _) = withContext(Dispatchers.IO) { ShizukuHelper.run(cmd) }
                val label = cmd.substringAfterLast(" ").take(30)
                tvBoostLog.append(if (ok) "✓ $label\n" else "! $label\n")
                delay(30)
            }

            btnFullBoost.isEnabled = true
            btnFullBoost.text = "✓ Aktif"
            pillStatus.text = "Full Boost"
            Toast.makeText(requireContext(), "Full Boost berhasil!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupModuleToggles(view: View) {
        val modules = mapOf(
            "fps" to R.id.sw_fps,
            "thermal" to R.id.sw_thermal,
            "network" to R.id.sw_network,
            "ram" to R.id.sw_ram,
            "graphics" to R.id.sw_graphics,
            "game" to R.id.sw_game
        )

        for ((id, resId) in modules) {
            val sw = view.findViewById<Switch>(resId) ?: continue
            moduleToggles[id] = sw
            sw.setOnCheckedChangeListener { _, checked ->
                if (!ShizukuHelper.isAvailable()) {
                    sw.isChecked = false
                    Toast.makeText(requireContext(), "Shizuku belum aktif", Toast.LENGTH_SHORT).show()
                    return@setOnCheckedChangeListener
                }
                val cmds = when (id) {
                    "fps" -> if (checked) ShizukuHelper.CMD_FPS_BOOST else ShizukuHelper.CMD_RESTORE
                    "thermal" -> ShizukuHelper.CMD_THERMAL
                    "network" -> ShizukuHelper.CMD_NETWORK
                    "ram" -> ShizukuHelper.CMD_RAM
                    "graphics" -> ShizukuHelper.CMD_GRAPHICS
                    "game" -> ShizukuHelper.CMD_GAME
                    else -> emptyList()
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    cmds.forEach { ShizukuHelper.run(it) }
                }
            }
        }
    }
}
