package com.kangrio.extension.hook.base

import de.robv.android.xposed.XC_MethodHook

interface IAbstractHook {
    fun init()
    fun hookAllMethods(hookClass: Class<*>, methodName: String, callback: XC_MethodHook)
    fun hookAllMethods(hookClassName: String, methodName: String, callback: XC_MethodHook)
    fun log(msg: Any)
}