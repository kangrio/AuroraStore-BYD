package com.kangrio.extension.hook

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

abstract class AbstractHook : IAbstractHook {
    open val TAG: String = "AbstractHook"

    override fun init() {
        log("Init")
    }

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

    companion object {
        init {
            XposedBridge.disableHiddenApiRestrictions()
        }
    }
}