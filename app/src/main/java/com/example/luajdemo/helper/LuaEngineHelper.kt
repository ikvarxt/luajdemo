package com.example.luajdemo.helper

import android.util.Log
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

private const val TAG = "LuaEngineHelper"

fun Globals.addPathForEntry(entryPath: String) {
    val luaPackage = get("package")
    val path = luaPackage.get("path").toString()
    val newPaths = listOf(
        path,
        "$entryPath/?.lua",
    )
    val resPath = newPaths.joinToString(";")
    Log.d(TAG, "addPathForEntry $resPath")
    luaPackage.set("path", LuaValue.valueOf(resPath))
}

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
        // lua index starts from 1
        this@luaValue.forEachIndexed { index, v -> set(index + 1, v.luaValue()) }
    }

    else -> LuaValue.valueOf(this.toString())
}

/**
 * without table conversion
 */
@Suppress("UNCHECKED_CAST")
fun <T> LuaValue.luaValueToKotlin(): T? {
    val v = try {
        when {
            isnil() -> null as T
            isboolean() -> toboolean() as T
            isint() -> toint() as T
            islong() -> tolong() as T
            isnumber() -> todouble() as T
            isstring() -> tojstring() as T
            else -> null // Return as LuaValue if type is not recognized
        }
    } catch (e: Exception) {
        Log.d(TAG, "luaValueToKotlin: ", e)
        null
    }
    return v
}

private fun LuaTable.toList(): List<Any?> {
    val result = mutableListOf<Any?>()
    for (i in 1..length()) {
        result.add(get(i).luaValueToKotlin())
    }
    return result
}

fun luaTableToKotlin(table: LuaTable): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()

    // Convert array-like part (1-based index)
    val listPart = table.toList()
    if (listPart.isNotEmpty()) {
        result["_array"] = listPart
    }

//    Log.d(TAG, "luaTableToKotlin: listPart=$listPart")

    // Convert hash part
    val keys = table.keys()
    for (j in keys.indices) {
        val key = keys[j]
        if (key.isnumber() && key.checkint() in 1..listPart.size) continue
        // TODO: 4/25/2025 ClassCastException when table has an invalid sequence array
        val k = key.luaValueToKotlin<String?>()
        if (k == null) {
            Log.d(TAG, "luaTableToKotlin: key is not string, ignore, $k")
            continue
        }
        result[k] = table[key].luaValueToKotlin()
    }

    return result
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
