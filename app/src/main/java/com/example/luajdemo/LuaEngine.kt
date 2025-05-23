package com.example.luajdemo

import android.util.Log
import com.example.luajdemo.helper.addPathForEntry
import com.example.luajdemo.helper.getStackTrace
import com.example.luajdemo.helper.luaValue
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.LibFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File

private const val TAG = "LuaEngine"

interface LuaEngine {

    fun initialize()

    fun loadEntryFile(path: File): Result<Unit>

    fun executeScript(script: String): Result<LuaValue>

    fun executeFunction(func: String, vararg args: Any?): Result<Varargs>
    fun executeFunctionSuppress(func: String, vararg args: Any?): Varargs

    fun loadLib(vararg lib: LibFunction)
    fun injectInstance(name: String, instance: Any)

    fun destroy()

}

class LuaEngineImpl : LuaEngine {

    private lateinit var runtime: Globals
    private val loadedLibs: MutableList<LibFunction> = mutableListOf()

    private lateinit var entryPath: String

    override fun initialize() {
        runtime = JsePlatform.debugGlobals()
    }

    override fun loadEntryFile(path: File): Result<Unit> = runCatching {
        with(runtime) {
            entryPath = path.parent!!
            set("entryPath", entryPath)
            addPathForEntry(entryPath)
            loadfile(path.absolutePath).call()
        }
    }

    override fun executeScript(script: String): Result<LuaValue> = runCatching {
        runtime.load(script).call()
    }

    override fun executeFunction(func: String, vararg args: Any?): Result<Varargs> = runCatching {
        val luaArgs = args.map { it.luaValue() }.toTypedArray()
        try {
            runtime.get(func).invoke(luaArgs)
        } catch (e: LuaError) {
            throw Error(runtime.getStackTrace(e))
        }
    }

    override fun executeFunctionSuppress(func: String, vararg args: Any?): Varargs {
        val luaArgs = args.map { it.luaValue() }.toTypedArray()
        return try {
            runtime.get(func).invoke(luaArgs)
        } catch (e: LuaError) {
            Log.e(TAG, "executeFunction: ${runtime.getStackTrace(e)}")
            LuaValue.NIL
        }
    }

    override fun loadLib(vararg lib: LibFunction) {
        loadedLibs += lib
        lib.forEach { runtime.load(it) }
    }

    override fun injectInstance(name: String, instance: Any) {
        Log.d(TAG, "injectInstance: $name")
        runtime.set(name, CoerceJavaToLua.coerce(instance))
    }

    override fun destroy() {
        loadedLibs.filterIsInstance<AutoCloseable>()
            .map { it.close() }
        loadedLibs.clear()
    }

}