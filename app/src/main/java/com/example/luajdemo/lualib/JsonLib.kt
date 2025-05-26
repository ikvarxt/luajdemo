package com.example.luajdemo.lualib

import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.io.File

class JsonLib(private val file: File) : TwoArgFunction() {

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val reader = file.inputStream().bufferedReader()
        env.checkglobals().load(reader, "json.lua")
        return env
    }
}