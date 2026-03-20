package com.vinz.flowx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.vinz.flowx.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etKey = view.findViewById<EditText>(R.id.et_api_key)
        val btnSave = view.findViewById<Button>(R.id.btn_save_key)
        val btnClear = view.findViewById<Button>(R.id.btn_clear_key)
        val tvStatus = view.findViewById<TextView>(R.id.tv_key_status)

        val prefs = requireContext().getSharedPreferences("flowx", 0)
        val existing = prefs.getString("gemini_key", "") ?: ""
        tvStatus.text = if (existing.isNotEmpty()) "✓ API key sudah diset" else "API key belum diset"

        btnSave.setOnClickListener {
            val key = etKey.text.toString().trim()
            if (key.isEmpty()) { Toast.makeText(requireContext(), "Masukkan API key dulu", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putString("gemini_key", key).apply()
            etKey.text.clear()
            tvStatus.text = "✓ API key tersimpan"
            Toast.makeText(requireContext(), "API key tersimpan", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            prefs.edit().remove("gemini_key").apply()
            tvStatus.text = "API key dihapus"
            Toast.makeText(requireContext(), "API key dihapus", Toast.LENGTH_SHORT).show()
        }
    }
}
