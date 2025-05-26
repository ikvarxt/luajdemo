package com.example.luajdemo

import org.junit.After
import org.junit.Before
import org.luaj.vm2.LuaValue

abstract class BaseEngineTest {

    protected lateinit var engine: LuaEngine

    @Before
    fun setup() {
        engine = LuaEngineImpl()
        engine.initialize()
        onInitialize(engine)
    }

    @After
    fun tearDown() {
        engine.destroy()
    }

    open fun onInitialize(engine: LuaEngine) {}

    protected fun run(script: String): LuaValue {
        return engine.executeScript(script).getOrThrow()
    }

}