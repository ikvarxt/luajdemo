package com.example.luajdemo.lualib

import android.content.Context
import android.content.SharedPreferences
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

class SharedPreferenceLib(private val context: Context) : TwoArgFunction() {

    private val defaultPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("lua_prefs", Context.MODE_PRIVATE)
    }

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val table = LuaValue.tableOf()

        // String operations
        table.set("getString", GetString())
        table.set("setString", SetString())

        // Int operations
        table.set("getInt", GetInt())
        table.set("setInt", SetInt())

        // Boolean operations
        table.set("getBoolean", GetBoolean())
        table.set("setBoolean", SetBoolean())

        // Float operations
        table.set("getFloat", GetFloat())
        table.set("setFloat", SetFloat())

        // Remove and clear operations
        table.set("remove", Remove())
        table.set("clear", Clear())

        // Set as module
        env.get("package").get("loaded").set("sp", table)
        // Set as global variable
        env.set("sp", table)
        return env
    }

    // String operations
    inner class GetString : TwoArgFunction() {
        override fun call(key: LuaValue, defaultValue: LuaValue): LuaValue {
            val result = defaultPrefs.getString(key.tojstring(), defaultValue.tojstring())
            return LuaValue.valueOf(result)
        }
    }

    inner class SetString : TwoArgFunction() {
        override fun call(key: LuaValue, value: LuaValue): LuaValue {
            defaultPrefs.edit().putString(key.tojstring(), value.tojstring()).apply()
            return LuaValue.NIL
        }
    }

    // Int operations
    inner class GetInt : TwoArgFunction() {
        override fun call(key: LuaValue, defaultValue: LuaValue): LuaValue {
            val result = defaultPrefs.getInt(key.tojstring(), defaultValue.toint())
            return LuaValue.valueOf(result.toDouble())
        }
    }

    inner class SetInt : TwoArgFunction() {
        override fun call(key: LuaValue, value: LuaValue): LuaValue {
            defaultPrefs.edit().putInt(key.tojstring(), value.toint()).apply()
            return LuaValue.NIL
        }
    }

    // Boolean operations
    inner class GetBoolean : TwoArgFunction() {
        override fun call(key: LuaValue, defaultValue: LuaValue): LuaValue {
            val result = defaultPrefs.getBoolean(key.tojstring(), defaultValue.toboolean())
            return LuaValue.valueOf(result)
        }
    }

    inner class SetBoolean : TwoArgFunction() {
        override fun call(key: LuaValue, value: LuaValue): LuaValue {
            defaultPrefs.edit().putBoolean(key.tojstring(), value.toboolean()).apply()
            return LuaValue.NIL
        }
    }

    // Float operations
    inner class GetFloat : TwoArgFunction() {
        override fun call(key: LuaValue, defaultValue: LuaValue): LuaValue {
            val result = defaultPrefs.getFloat(key.tojstring(), defaultValue.tofloat())
            return LuaValue.valueOf(result.toDouble())
        }
    }

    inner class SetFloat : TwoArgFunction() {
        override fun call(key: LuaValue, value: LuaValue): LuaValue {
            defaultPrefs.edit().putFloat(key.tojstring(), value.tofloat()).apply()
            return LuaValue.NIL
        }
    }

    // Remove and clear operations
    inner class Remove : OneArgFunction() {
        override fun call(key: LuaValue): LuaValue {
            defaultPrefs.edit().remove(key.tojstring()).apply()
            return LuaValue.NIL
        }
    }

    inner class Clear : ZeroArgFunction() {
        override fun call(): LuaValue {
            defaultPrefs.edit().clear().apply()
            return LuaValue.NIL
        }
    }
}