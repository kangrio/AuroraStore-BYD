package com.kangrio.extension

import com.kangrio.extension.hook.base.AbstractHook
import de.robv.android.xposed.XposedBridge

object MainHook {

    fun getHookClasses(): List<Class<out AbstractHook>> {
        val classes = mutableListOf<Class<out AbstractHook>>()
        var number = 1
        val maxNumber = 100
        while (number <= maxNumber) {
            try {
                val clazz = Class.forName("com.kangrio.extension.hook.Hook$number")
                if (AbstractHook::class.java.isAssignableFrom(clazz)) {
                    classes.add(clazz as Class<out AbstractHook>)
                }
            } catch (e: ClassNotFoundException) {
                return classes
            }
            number++
        }
        return classes
    }

    fun init() {
        XposedBridge.disableHiddenApiRestrictions()
        getHookClasses().forEach {
            it.newInstance().init()
        }
    }
}