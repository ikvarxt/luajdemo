package com.example.luajdemo.lualib

import com.example.luajdemo.BaseEngineTest
import com.example.luajdemo.LuaEngine
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
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
    fun `check http lib exists`() {
        val result = run("return http")
        assertTrue(result.istable())
        assertTrue(result.get("request").isfunction())
    }

    // region get

    @Test
    fun `get with no args`() {
        assertFailsWith<LuaError> {
            run("return http.request()")
        }
    }

    @Test
    fun `simple get request, default method get`() {
        val res = run(
            """
            return http.request('$base/get')
            """.trimIndent()
        )
        assertTrue(res.checkjstring().contains("$base/get"))
    }

    @Test
    fun `get request with headers`() {
        val res = run(
            """
            local json = require('json')
            local r = http.request({ url = '$base/get', headers = { a = 'a', b = 'b' }})
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
            return http.request({ 
                url = '$base/post', 
                method = "POST", 
                body = 'my lua request body'
            })
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
            local r = http.request({ 
                url = '$base/post', 
                method = 'POST',
                body = '', 
                headers = { a = 'a', b = 'b' }
            })
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
            run("return http.request({ method = 'POST' })")
        }
    }

    @Test
    fun `post without body`() {
        assertFailsWith<LuaError> {
            run("return http.request({ url = '$base/post', method = 'POST' })")
        }
    }
    // endregion

    // region status code

    private fun assertFailedStatusCode(r: LuaValue, code: Int) {
        assertTrue(r.isstring(), "string, $code")
        assertTrue(r.checkjstring().contains("$code"), "$code")
    }

    @Test
    fun `get with invalid url 404`() {
        val getR = run(
            """
            local r, e = http.request('$base/status/404')
            assert(r == nil)
            return e
            """.trimIndent()
        )
        assertFailedStatusCode(getR, 404)

        val postR = run(
            """
            local r, e = http.request({ 
                url = '$base/status/404', 
                method = 'POST',
                body = '' 
            })
            assert(r == nil)
            return e
            """.trimIndent()
        )
        assertFailedStatusCode(postR, 404)
    }

    @Test
    fun `get with status code 500`() {
        val getR = run(
            """
            local r, e = http.request('$base/status/500')
            assert(r == nil)
            return e
        """.trimIndent()
        )
        assertFailedStatusCode(getR, 500)

        val postR = run(
            """
            local r, e = http.request({ 
                url = '$base/status/500', 
                method = 'POST',
                body = '' 
            })
            assert(r == nil)
            return e
            """.trimIndent()
        )
        assertFailedStatusCode(postR, 500)
    }
    // endregion
}