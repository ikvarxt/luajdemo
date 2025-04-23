package com.example.luajdemo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.luajdemo.helper.copyToExternal
import com.example.luajdemo.lualib.AndroidLib

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val engine = LuaEngineImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        copyAssets()

        engine.initialize()
        engine.loadLib(AndroidLib(this.applicationContext))

        engine.loadEntryFile(externalFile().resolve("lua").resolve("main.lua"))
            .onFailure { textView.text = "load: " + it.stackTraceToString() }
            .onFailure { return }

        engine.executeFunction("hello")
            .onSuccess { textView.text = it.toString() }
            .onFailure { textView.text = "call: " + it.stackTraceToString() }
    }

    private fun initView() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun externalFile() = getExternalFilesDir(null)!!

    private fun copyAssets() {
        assets.copyToExternal(
            "lua",
            getExternalFilesDir(null)!!.resolve("lua")
        )
    }
}