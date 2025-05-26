package com.example.luajdemo.lualib

import com.example.luajdemo.helper.luaValue
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction

class NetworkLib : TwoArgFunction() {

    private lateinit var client: OkHttpClient

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        initializeOkhttpClient()
        val lib = LuaValue.tableOf()

        lib.set("get", Get())

        env.set("network", lib)
        env.get("package").get("loaded").set("network", lib)
        return env
    }

    private fun initializeOkhttpClient() {
        client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    inner class Get : VarArgFunction() {

        override fun invoke(args: Varargs): LuaValue {
            val url = args.arg(1).checkstring().tojstring()
            val params = args.arg(2)

            val req = Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(params.parseHeaders())
                .build()

            client.newCall(req).execute().use { response ->
                return response.body?.string().luaValue()
            }
        }
    }

    inner class Post : VarArgFunction() {

        override fun invoke(args: Varargs?): Varargs {
            return super.invoke(args)
        }
    }

    private fun LuaValue.parseHeaders(): Headers {
        val headersBuilder = Headers.Builder()
        val paramsTable = if (istable()) checktable() else tableOf()
        val headers = paramsTable.get("headers")
        if (headers.istable()) {
            val table = headers.checktable()
            table.keys()
                .map { it.tojstring() }
                .forEach { key ->
                    headersBuilder.add(key, table.get(key).tojstring())
                }
        }
        return headersBuilder.build()
    }


}