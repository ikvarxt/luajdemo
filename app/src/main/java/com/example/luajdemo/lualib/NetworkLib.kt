package com.example.luajdemo.lualib

import com.example.luajdemo.helper.luaValue
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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
        lib.set("post", Post())
        lib.set("request", HttpRequest())

        env.set("http", lib)
        env.get("package").get("loaded").set("http", lib)
        return env
    }

    private fun initializeOkhttpClient() {
        client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    inner class HttpRequest : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() == 0) {
                return argerror("no arg provided")
            }
            val params = args.arg(1)
            val url =
                if (params.isstring()) params.checkjstring()
                else params.get("url").checkjstring()

            val method =
                if (params.get("method").isnil()) "GET"
                else params.get("method").checkjstring()
            val body = params.parseRequestBody()
            val headers = params.parseHeaders()

            try {
                val req = Request.Builder()
                    .url(url)
                    .method(method, body)
                    .headers(headers)
                    .build()

                return client.newCall(req).execute().parseResult()
            } catch (e: Exception) {
                return error(e.message ?: "unknown error")
            }
        }
    }

    inner class Get : VarArgFunction() {

        override fun invoke(args: Varargs): Varargs {
            val url = args.arg(1).checkstring().tojstring()
            val params = args.arg(2)

            val req = Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(params.parseHeaders())
                .build()

            return client.newCall(req).execute().parseResult()
        }
    }

    inner class Post : VarArgFunction() {

        override fun invoke(args: Varargs): Varargs {
            val url = args.arg(1).checkstring().tojstring()
            val params = args.arg(2)

            try {
                val req = Request.Builder()
                    .url(url)
                    .method("POST", params.parseRequestBody())
                    .headers(params.parseHeaders())
                    .build()

                return client.newCall(req).execute().parseResult()
            } catch (e: Exception) {
                return error(e.message ?: "unknown error")
            }
        }
    }

    companion object {

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

        private fun LuaValue.parseRequestBody(): RequestBody? {
            val requestBody: RequestBody
            val paramsTable = if (istable()) checktable() else tableOf()
            val bodyValue = paramsTable.get("body")
            if (bodyValue.isstring()) {
                requestBody = bodyValue.checkjstring().toRequestBody("application/json".toMediaTypeOrNull())
            } else {
                return null
            }
            return requestBody
        }

        private fun Response?.parseResult(): Varargs = use {
            if (this == null) {
                return varargsOf(NIL, valueOf("fatal client response is null"))
            }
            if (isSuccessful.not()) {
                val error = valueOf("failed code: $code, message: $message")
                return varargsOf(NIL, error)
            }
            val resString = body?.string()
            return varargsOf(resString.luaValue(), NIL)
        }
    }

}