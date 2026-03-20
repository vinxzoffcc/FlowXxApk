package com.vinz.flowx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.vinz.flowx.R

class AiFragment : Fragment() {

    private lateinit var tvChat: TextView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var scrollView: ScrollView
    private val chatHistory = mutableListOf<JSONObject>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_ai, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvChat = view.findViewById(R.id.tv_chat)
        etInput = view.findViewById(R.id.et_chat_input)
        btnSend = view.findViewById(R.id.btn_send)
        scrollView = view.findViewById(R.id.scroll_chat)

        appendChat("FlowX AI", "Yo! Saya FlowX AI. Tanya soal game lag, HP panas, cara boost — gas aja.")

        btnSend.setOnClickListener {
            val msg = etInput.text.toString().trim()
            if (msg.isEmpty()) return@setOnClickListener
            etInput.text.clear()
            appendChat("Kamu", msg)
            sendToGemini(msg)
        }
    }

    private fun sendToGemini(message: String) {
        val prefs = requireContext().getSharedPreferences("flowx", 0)
        val apiKey = prefs.getString("gemini_key", "") ?: ""
        if (apiKey.isEmpty()) {
            appendChat("FlowX AI", "API key belum diset. Isi dulu di tab Setelan.")
            return
        }

        btnSend.isEnabled = false
        appendChat("FlowX AI", "...")

        val userPart = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", message)))
        }
        chatHistory.add(userPart)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val system = "Kamu FlowX AI, asisten gaming dari app FlowX buatan Vinz. Santai, bahasa Indonesia sehari-hari. Spesialis optimasi HP Android untuk FF, PUBG, ML, Genshin. Jawab singkat dan to the point."

                val body = JSONObject().apply {
                    put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                    put("contents", JSONArray(chatHistory.toList()))
                }.toString()

                conn.outputStream.write(body.toByteArray())

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val reply = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                chatHistory.add(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", reply)))
                })

                withContext(Dispatchers.Main) {
                    // Remove "..."
                    val current = tvChat.text.toString()
                    tvChat.text = current.replace("\nFlowX AI: ...\n", "")
                    appendChat("FlowX AI", reply)
                    btnSend.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val current = tvChat.text.toString()
                    tvChat.text = current.replace("\nFlowX AI: ...\n", "")
                    appendChat("FlowX AI", "Error: ${e.message}")
                    btnSend.isEnabled = true
                }
            }
        }
    }

    private fun appendChat(sender: String, msg: String) {
        tvChat.append("\n$sender: $msg\n")
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}
