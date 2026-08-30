package com.kangrio.extension.hook.base

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

abstract class AbstractHook : IAbstractHook {
    abstract val TAG: String

    abstract override fun init()

    override fun hookAllMethods(hookClass: Class<*>, methodName: String, callback: XC_MethodHook) {
        try {
            XposedBridge.hookAllMethods(hookClass, methodName, callback)
            log("Hooked " + hookClass.getName() + "." + methodName)
        } catch (e: Throwable) {
            log(e)
        }
    }

    override fun hookAllMethods(hookClassName: String, methodName: String, callback: XC_MethodHook) {
        try {
            val hookClass = Class.forName(hookClassName)
            hookAllMethods(hookClass, methodName, callback)
        } catch (e: Throwable) {
            log(e)
        }
    }

    override fun log(msg: Any) {
        Log.v(TAG, msg.toString())
    }
}