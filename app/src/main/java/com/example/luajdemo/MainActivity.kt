package com.example.luajdemo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.luajdemo.helper.copyTo
import com.example.luajdemo.helper.luaTableToKotlin
import com.example.luajdemo.lualib.AndroidLib
import com.example.luajdemo.lualib.EventBusLib
import com.example.luajdemo.lualib.SharedPreferenceLib
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.luaj.vm2.LuaTable

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val engine: LuaEngine = LuaEngineImpl()
    private val eventBus = initEventBus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        try {
            copyAssets()
        } catch (e: Exception) {
            appendError(e, "copy assets failed")
            Log.e(TAG, "copy assets failed", e)
            return
        }

        engine.initialize()
        engine.loadLib(
            AndroidLib(this.applicationContext),
            SharedPreferenceLib(this.applicationContext),
            EventBusLib(eventBus),
        )

        engine.loadEntryFile(externalFile().resolve("lua").resolve("main.lua"))
            .onFailure { appendError(it, "load file") }
            .onFailure { return }

        engine.executeFunction("hello")
            .onSuccess { appendText(it.toString()) }
            .onFailure { appendError(it, "hello") }

        engine.executeFunction("test_sp")
            .onSuccess { appendText(it.toString()) }
            .onFailure { appendError(it, "test_sp") }

        engine.executeFunction("getMyTable")
            .onSuccess {
                if (it is LuaTable) appendText(luaTableToKotlin(it).toString())
                else appendText(it.toString())
            }
            .onFailure { appendError(it, "getMyTable") }

        lifecycleScope.launch {
            launch {
                eventBus.register("ui").collect {
                    appendText(it.toString())
                }
            }
            launch {
                eventBus.register("ping").collect {
                    Log.d(TAG, "native ping, ${it.toString()}")
                }
            }
        }

        engine.executeFunction("testEventBus")
            .onSuccess {
                lifecycleScope.launch {
                    repeat(5) {
                        eventBus.post("ping", "pong")
                        delay(1000)
                    }
                }
            }
            .onFailure { appendError(it, "registerEvent") }

        engine.executeFunction("readAndroidFile")
            .onFailure { appendError(it, "readAndroidFile") }

        engine.executeFunction("TEST_JSON_ENCODE")
            .onSuccess { appendText(it.toString()) }
            .onFailure { appendError(it, "TEST_JSON_ENCODE") }

        engine.executeFunction("TEST_JSON_DECODE")
            .onSuccess { appendText(luaTableToKotlin(it as LuaTable).toString()) }
            .onFailure { appendError(it, "TEST_JSON_DECODE") }

        engine.executeFunction("TEST_FILE_TO_JSON")
            .onSuccess { appendText(luaTableToKotlin(it as LuaTable).toString()) }
            .onFailure { appendError(it, "TEST_FILE_TO_JSON") }

        engine.executeScript("android.loginfo('luaa', 'hello world')")
            .onSuccess { appendText("loged hello world") }
            .onFailure { appendError(it, "executeScript") }

        data class TestClass(val name: String, var age: Int) {
            fun hello() = "hello from test class, $name, age=$age"
        }

        val t = TestClass("abc", 12)

        engine.injectInstance("testClass", t)

        engine.executeScript("return { h = testClass:hello(), a = testClass.age }")
            .onSuccess { appendText(luaTableToKotlin(it as LuaTable).toString()) }
            .onFailure { appendError(it, "executeScript get injected class") }

        engine.executeScript("testClass:setAge(280)")
            .onFailure { appendError(it, "executeScript setAge from lua") }

        // note: lua engine can't directly read kotlin field, must through getters
        engine.executeScript("return { h = testClass:hello(), a = testClass:getAge() }")
            .onSuccess { appendText(luaTableToKotlin(it as LuaTable).toString()) }
            .onFailure { appendError(it, "executeScript get injected class property changed") }

        engine.executeFunction("TEST_CROSS_FOLDER_REQUIRE")
            .onFailure { appendError(it, "TEST_CROSS_FOLDER_REQUIRE") }

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

    private fun appendText(text: String) {
        textView.append("$text\n\n")
    }

    private fun appendError(e: Throwable, msg: String? = null) {
        val redFontText = buildSpannedString {
            append("$msg ${e.stackTraceToString()}\n\n")
            setSpan(android.text.style.ForegroundColorSpan(Color.RED), 0, length, 0)
        }
        textView.append(redFontText)
    }

    private fun externalFile() = getExternalFilesDir(null)!!

    private fun copyAssets() {
        val targetFile = getExternalFilesDir(null)!!.resolve("lua")
        if (targetFile.exists()) {
            targetFile.deleteRecursively()
        }
        assets.copyTo("lua", targetFile)
    }

    private fun initEventBus() = EventBus()

    override fun onDestroy() {
        super.onDestroy()
        engine.destroy()
    }

}