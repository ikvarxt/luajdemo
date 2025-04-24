package com.example.luajdemo.lualib

import com.example.luajdemo.EventBus
import com.example.luajdemo.helper.luaTableToKotlin
import com.example.luajdemo.helper.luaValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import kotlin.coroutines.CoroutineContext

class EventBusLib(private val bus: EventBus) : TwoArgFunction(), CoroutineScope, AutoCloseable {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    override fun call(modname: LuaValue, env: LuaValue): LuaValue {
        val table = LuaValue.tableOf()

        table["post"] = post()
        table["register"] = register()

        env.get("package").get("loaded").set("eventbus", table)
        return env
    }

    inner class post : TwoArgFunction() {
        override fun call(event: LuaValue, param: LuaValue): LuaValue {
            if (!event.isstring() || !param.istable()) {
                return LuaValue.error("event must be string and param must be table")
            }
            val e = event.tojstring()
            bus.post(e, luaTableToKotlin(param.checktable()))
            return NONE
        }
    }

    inner class register : TwoArgFunction() {
        override fun call(eventName: LuaValue, callback: LuaValue): LuaValue {
            if (!eventName.isstring() || !callback.isfunction()) {
                return LuaValue.error("event must be string and callback must be function")
            }
            launch {
                bus.register(eventName.tojstring()).collect {
                    callback.invoke(it.luaValue())
                }
            }
            return NONE
        }
    }

    override fun close() {
        cancel()
    }

}