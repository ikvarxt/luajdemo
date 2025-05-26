package com.example.luajdemo.lualib

import com.example.luajdemo.BaseEngineTest
import com.example.luajdemo.LuaEngine
import org.luaj.vm2.LuaError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetworkLibTest : BaseEngineTest() {

    override fun onInitialize(engine: LuaEngine) {
        engine.loadLib(NetworkLib())
    }

    @Test
    fun `json without load json`() {
        assertFails {
            val r = run(
                """
                local json = require('json')
                return json
            """.trimIndent()
            )
            assertTrue(r.istable())
            val jsonLib = r.checktable()
            assertTrue(jsonLib.get("encode").isfunction())
            assertTrue(jsonLib.get("decode").isfunction())
        }
    }

    @Test
    fun `check network lib exists`() {
        val result = run("return network")
        assertTrue(result.istable())
        assertTrue(result.get("get").isfunction())
    }

    @Test
    fun `get with no args`() {
        assertFailsWith<LuaError> {
            run("return network.get()")
        }
    }

    @Test
    fun `simple get request`() {
        val res = run(
            """
            return network.get('https://httpbin.org/get')
            """.trimIndent()
        )
        assertTrue(res.checkjstring().contains("https://httpbin.org/get"))
    }

    @Test
    fun `get request with headers`() {
        val res = run(
            """
            local json = require('json')
            local r = network.get('https://httpbin.org/get', { headers = { a = 'a', b = 'b' }})
            return json.decode(r)
            """.trimIndent()
        )
        assertTrue(res.istable(), "table")
        val table = res.checktable()
        val headers = table["headers"]
        assertEquals("a", headers["A"].checkjstring())
        assertEquals("b", headers["B"].checkjstring())
    }
}