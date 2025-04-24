package com.example.luajdemo.lualib

import android.content.Context
import android.util.Log
import com.example.luajdemo.helper.luaValue
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

class AndroidLib(
    private val context: Context,
) : TwoArgFunction() {

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val table = LuaTable()
        table.set("externalFilePath", externalFilePath())
        table.set("loginfo", loginfo())

        // set as module
        env.get("package").get("loaded").set("android", table)
        // set as global variable
        env.set("android", table)
        return env
    }

    inner class loginfo : TwoArgFunction() {
        override fun call(tag: LuaValue, msg: LuaValue): LuaValue {
            Log.d(tag.tojstring(), msg.tojstring())
            return NONE
        }
    }

    inner class externalFilePath : ZeroArgFunction() {
        override fun call(): LuaValue {
            val file = context.getExternalFilesDir(null)
                ?: return LuaValue.error("no external files dir, SdCard not mounted")
            return file.luaValue()
        }
    }

}