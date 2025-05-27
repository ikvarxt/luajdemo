package com.example.luajdemo.lualib

import com.example.luajdemo.BaseEngineTest
import com.example.luajdemo.LuaEngine
import org.luaj.vm2.LuaError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetworkLibTest : BaseEngineTest() {

    private var base = "https://httpbin.org"

    override fun onInitialize(engine: LuaEngine) {
        engine.loadLib(NetworkLib())
    }

    @Test
    fun `check network lib exists`() {
        val result = run("return network")
        assertTrue(result.istable())
        assertTrue(result.get("get").isfunction())
    }

    // region get

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
            return network.get('$base/get')
            """.trimIndent()
        )
        assertTrue(res.checkjstring().contains("$base/get"))
    }

    @Test
    fun `get request with headers`() {
        val res = run(
            """
            local json = require('json')
            local r = network.get('$base/get', { headers = { a = 'a', b = 'b' }})
            return json.decode(r)
            """.trimIndent()
        )
        assertTrue(res.istable(), "table")
        val table = res.checktable()
        val headers = table["headers"]
        assertEquals("a", headers["A"].checkjstring())
        assertEquals("b", headers["B"].checkjstring())
    }
    // endregion

    // region post
    @Test
    fun `post simple request`() {
        val res = run(
            """
            return network.post('$base/post', {body = 'my lua request body'})
            """.trimIndent()
        )
        assertTrue(res.checkjstring().contains("$base/post"))
        assertTrue(res.checkjstring().contains("my lua request body"), res.checkjstring())
    }

    @Test
    fun `post request with headers`() {
        val res = run(
            """
            local json = require('json')
            local r = network.post('$base/post', { body = '', headers = { a = 'a', b = 'b' }})
            return json.decode(r)
            """.trimIndent()
        )
        assertTrue(res.istable(), "table")
        val table = res.checktable()
        val headers = table["headers"]
        assertEquals("a", headers["A"].checkjstring())
        assertEquals("b", headers["B"].checkjstring())
    }

    @Test
    fun `post with no args`() {
        assertFailsWith<LuaError> {
            run("return network.post()")
        }
    }
    // endregion

    // region status code
    @Test
    fun `get with invalid url 404`() {
        assertFailsWith<LuaError>("get") {
            run("return network.get('$base/status/404')")
        }.also {
            assertEquals("failed code: 404, message: ", it.message)
        }
        assertFailsWith<LuaError>("post") {
            run("return network.post('$base/status/404', { body = '' })")
        }.also {
            assertEquals("failed code: 404, message: ", it.message)
        }
    }

    @Test
    fun `get with status code 500`() {
        assertFailsWith<LuaError>("get") {
            run("return network.get('$base/status/500')")
        }.also {
            assertEquals("failed code: 500, message: ", it.message)
        }
        assertFailsWith<LuaError>("post") {
            run("return network.post('$base/status/500', { body = '' })")
        }.also {
            assertEquals("failed code: 500, message: ", it.message)
        }
    }
    // endregion
}