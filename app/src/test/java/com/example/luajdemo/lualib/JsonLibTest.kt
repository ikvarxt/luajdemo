package com.example.luajdemo.lualib

import com.example.luajdemo.BaseEngineTest
import com.example.luajdemo.LuaEngine
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonLibTest : BaseEngineTest() {

    override fun onInitialize(engine: LuaEngine) {
        val file = javaClass.classLoader?.getResource("json.lua")?.file?.let { File(it) }
            ?: throw IllegalStateException("json.lua not found")
        engine.loadLib(JsonLib(file))
    }

    @Test
    fun `json lib is exists`() {
        val r = run(
            """
            local json = require('json')
            return json
        """.trimIndent()
        )
        assertTrue(r.istable(), r.toString())
    }

    @Test
    fun `encode json`() {
        val res = run(
            """
            local json = require('json')
            return json.encode({ a = 'abc', b = true, c = 123, d = nil, e = 3.14})
            """.trimIndent()
        )
        assertTrue(res.checkjstring().contains("abc"))
        assertTrue(res.checkjstring().contains("true"))
    }

}