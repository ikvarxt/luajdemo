package com.example.luajdemo

import android.util.Log
import com.example.luajdemo.helper.addPathForEntry
import com.example.luajdemo.helper.luaValue
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.LibFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File

private const val TAG = "LuaEngine"

interface LuaEngine {

    fun initialize()

    fun loadEntryFile(path: File): Result<Unit>

    fun executeFunction(func: String, vararg args: Any?): Result<Varargs>
    fun executeFunctionSuppress(func: String, vararg args: Any?): Varargs

    fun loadLib(vararg lib: LibFunction)

}

class LuaEngineImpl : LuaEngine {

    private lateinit var runtime: Globals

    private lateinit var entryPath: String

    override fun initialize() {
        runtime = JsePlatform.debugGlobals()
        runtime["error"] = LuaValue.valueOf("debug.traceback")
    }

    override fun loadEntryFile(path: File): Result<Unit> = runCatching {
        with(runtime) {
            entryPath = path.parent!!
            addPathForEntry(entryPath)
            loadfile(path.absolutePath).call()
        }
    }

    override fun executeFunction(func: String, vararg args: Any?): Result<Varargs> = runCatching {
        val luaArgs = args.map { it.luaValue() }.toTypedArray()
        runtime.get(func).invoke(luaArgs)
    }

    override fun executeFunctionSuppress(func: String, vararg args: Any?): Varargs {
        val luaArgs = args.map { it.luaValue() }.toTypedArray()
        return try {
            runtime.get(func).invoke(luaArgs)
        } catch (e: LuaError) {
            Log.e(TAG, "executeFunction: ${getStackTrace(e)}")
            LuaValue.NIL
        }
    }

    override fun loadLib(vararg lib: LibFunction) {
        lib.forEach { runtime.load(it) }
    }

    private fun getStackTrace(error: LuaError): String {
        return try {
            // Get the debug library
            val debug = runtime.get("debug")
            // Call traceback with the error message
            val trace = debug.get("traceback").call(LuaValue.valueOf(error.message ?: ""))
            trace.tojstring().replace("$entryPath/", "")
        } catch (e: Exception) {
            "Failed to get stack trace: ${e.message}"
        }
    }

}