package com.kangrio.extension

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.content.pm.IPackageManager
import android.os.ServiceManager

@SuppressLint("DiscouragedPrivateApi")
class SpoofInstallerSource {
    companion object {
        fun init() {
            getInstallerPackageName()
        }

        fun getInstallerPackageName() {
            val myPackageManager = PackageManagerProxy()
            val iPackageManager = IPackageManager.Stub.asInterface(myPackageManager)
            val sCache = ServiceManager::class.java.getDeclaredField("sCache").let {
                it.isAccessible = true
                it.get(null) as MutableMap<String, Any>
            }
            sCache.clear()
            sCache["package"] = myPackageManager

            val field = ActivityThread::class.java
                .getDeclaredField("sPackageManager")
                .apply {
                    isAccessible = true
                }
            field.set(null, iPackageManager)
        }
    }
}