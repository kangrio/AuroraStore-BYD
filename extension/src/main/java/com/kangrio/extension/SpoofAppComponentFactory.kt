package com.kangrio.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppComponentFactory
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@SuppressLint("NewApi")
class SpoofAppComponentFactory: AppComponentFactory() {
    var coreComponentFactory: Class<*>? = null
    var  checkCompatWrapper: Method? = null

    init {
        try {
            HiddenApiBypass.addHiddenApiExemptions("L")
            coreComponentFactory = Class.forName("androidx.core.app.CoreComponentFactory")
            checkCompatWrapper = coreComponentFactory?.declaredMethods?.firstOrNull { m ->
                Modifier.isStatic(m.modifiers) &&       // static
                m.parameterCount == 1 &&                       // 1 param
                m.parameterTypes[0] == Any::class.java &&      // param = Object
                m.returnType == Any::class.java                // return = Object (due to type erasure)
            }?.apply {
                isAccessible = true
            }

        }catch (_: Throwable) {}

        SpoofUtil.killPM()
    }

    override fun instantiateActivity(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): Activity {
        return checkCompatWrapper(super.instantiateActivity(cl, className, intent))
    }

    override fun instantiateApplication(cl: ClassLoader, className: String): Application {
        return checkCompatWrapper(super.instantiateApplication(cl, className))
    }

    override fun instantiateClassLoader(cl: ClassLoader, aInfo: ApplicationInfo): ClassLoader {
        return checkCompatWrapper(super.instantiateClassLoader(cl, aInfo))
    }

    override fun instantiateProvider(cl: ClassLoader, className: String): ContentProvider {
        return checkCompatWrapper(super.instantiateProvider(cl, className))
    }

    override fun instantiateReceiver(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): BroadcastReceiver {
        return checkCompatWrapper(super.instantiateReceiver(cl, className, intent))
    }

    override fun instantiateService(cl: ClassLoader, className: String, intent: Intent?): Service {
        return checkCompatWrapper(super.instantiateService(cl, className, intent))
    }

    fun <T> checkCompatWrapper(obj: T): T {
        checkCompatWrapper ?: return obj
        var obj: T = obj
        try {
            obj = checkCompatWrapper?.invoke(null, obj) as T ?: obj

            return obj
        } catch (e: Throwable) {
            Log.w("AppFactory", $$"CoreComponentFactory$CompatWrapped is not being used", e)
        }
        return obj
    }
}