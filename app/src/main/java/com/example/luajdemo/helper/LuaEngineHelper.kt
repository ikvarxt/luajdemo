package com.example.luajdemo.helper

import android.util.Log
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue

private const val TAG = "LuaEngineHelper"

fun Globals.addPathForEntry(entryPath: String) {
    val luaPackage = get("package")
    val path = luaPackage.get("path").toString()
    val newPaths = listOf(
        path,
        "$entryPath/?.lua",
        // "$entryPath/?/?.lua",
        // "$entryPath/*/?.lua"
    )
    val resPath = newPaths.joinToString(";")
    Log.d(TAG, "addPathForEntry $resPath")
    luaPackage.set("path", LuaValue.valueOf(resPath))
}

/**
 * 将 Kotlin 对象转换为 Lua 值。
 * @param value 要转换的对象
 * @return 转换后的 Lua 值
 */
fun Any?.luaValue(): LuaValue = when (this) {
    null -> LuaValue.NIL
    is String -> LuaValue.valueOf(this)
    is Int -> LuaValue.valueOf(this)
    is Double -> LuaValue.valueOf(this)
    is Float -> LuaValue.valueOf(this.toDouble())
    is Boolean -> LuaValue.valueOf(this)
    is Long -> LuaValue.valueOf(this.toDouble())
    is Number -> LuaValue.valueOf(this.toDouble())
    is Map<*, *> -> LuaValue.tableOf().apply {
        this@luaValue.forEach { (k, v) -> set(k.toString(), v.luaValue()) }
    }

    is List<*> -> LuaValue.tableOf().apply {
        this@luaValue.forEachIndexed { index, v -> set(index + 1, v.luaValue()) }
    }

    else -> LuaValue.valueOf(this.toString())
}

fun Globals.getStackTrace(error: LuaError): String {
    return try {
        // Get the debug library
        val debug = get("debug")
        if (debug == LuaValue.NIL) {
            return "Failed to get debug library"
        }
        val entryPathValue = get("entryPath")
        val entryPath =
            if (entryPathValue == LuaValue.NIL) ""
            else entryPathValue.tojstring().trimEnd('/') + '/'
        // Call traceback with the error message
        val trace = debug.get("traceback").call(LuaValue.valueOf(error.message ?: ""))
        trace.tojstring().replace(entryPath, "")
    } catch (e: Exception) {
        "Failed to get stack trace: ${e.message}"
    }
}
