package com.vinz.flowx.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.vinz.flowx.MainActivity
import com.vinz.flowx.R
import com.vinz.flowx.service.OverlayService

class HudFragment : Fragment() {

    private var btnToggleHud: Button? = null
    private var swCpu: Switch? = null
    private var swRam: Switch? = null
    private var swBat: Switch? = null
    private var swTmp: Switch? = null
    private var swFps: Switch? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_hud, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            btnToggleHud = view.findViewById(R.id.btn_toggle_hud)
            swCpu = view.findViewById(R.id.sw_hud_cpu)
            swRam = view.findViewById(R.id.sw_hud_ram)
            swBat = view.findViewById(R.id.sw_hud_bat)
            swTmp = view.findViewById(R.id.sw_hud_tmp)
            swFps = view.findViewById(R.id.sw_hud_fps)

            updateButton()

            btnToggleHud?.setOnClickListener {
                try {
                    if (!Settings.canDrawOverlays(requireContext())) {
                        Toast.makeText(requireContext(), "Aktifkan izin Draw Over Apps di Setelan", Toast.LENGTH_LONG).show()
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        return@setOnClickListener
                    }
                    if (OverlayService.isActive) {
                        (activity as? MainActivity)?.stopOverlayService()
                    } else {
                        (activity as? MainActivity)?.startOverlayService()
                    }
                    view.postDelayed({ updateButton() }, 500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            swCpu?.isChecked = OverlayService.showCpu
            swRam?.isChecked = OverlayService.showRam
            swBat?.isChecked = OverlayService.showBat
            swTmp?.isChecked = OverlayService.showTmp
            swFps?.isChecked = OverlayService.showFps

            swCpu?.setOnCheckedChangeListener { _, c -> OverlayService.showCpu = c }
            swRam?.setOnCheckedChangeListener { _, c -> OverlayService.showRam = c }
            swBat?.setOnCheckedChangeListener { _, c -> OverlayService.showBat = c }
            swTmp?.setOnCheckedChangeListener { _, c -> OverlayService.showTmp = c }
            swFps?.setOnCheckedChangeListener { _, c -> OverlayService.showFps = c }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateButton() {
        try {
            if (OverlayService.isActive) {
                btnToggleHud?.text = "Matikan HUD"
                context?.let { btnToggleHud?.setBackgroundColor(it.getColor(R.color.red)) }
            } else {
                btnToggleHud?.text = "Aktifkan HUD"
                context?.let { btnToggleHud?.setBackgroundColor(it.getColor(R.color.accent)) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        updateButton()
    }
}
