package com.kangrio.extension

import android.app.ActivityThread
import android.content.pm.IPackageManager
import android.content.pm.InstallSourceInfo
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.lang.reflect.Field
import java.lang.reflect.Proxy

class SpoofInstallerSource {
    companion object {
        const val PLAY_STORE_PACKAGE_NAME = "com.android.vending"
        fun init() {
            getInstallerPackageName()
            installSourceInfo()
        }

        fun getInstallerPackageName() {
            val field = ActivityThread::class.java
                .getDeclaredField("sPackageManager")
                .apply {
                    isAccessible = true
                }


            val original = field.get(null) ?: ActivityThread.getPackageManager()

            val proxy = Proxy.newProxyInstance(
                IPackageManager::class.java.classLoader,
                arrayOf(IPackageManager::class.java)
            ) { _, method, args ->

                if (method.name.equals("getInstallerPackageName")) {
                    PLAY_STORE_PACKAGE_NAME
                } else {
                    method.invoke(original, *(args ?: emptyArray()))
                }
            }

            field.set(null, proxy)
        }

        fun installSourceInfo(){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

            val originalCreator = InstallSourceInfo.CREATOR
            val creator = object : Parcelable.Creator<InstallSourceInfo> {
                override fun createFromParcel(source: Parcel): InstallSourceInfo? {
                    source.recycle()

                    // https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/content/pm/InstallSourceInfo.java#71
                    val newSource = Parcel.obtain()
                    newSource.writeString(PLAY_STORE_PACKAGE_NAME)     // mInitiatingPackageName
                    newSource.writeParcelable(null, 0)  // mInitiatingPackageSigningInfo
                    newSource.writeString(PLAY_STORE_PACKAGE_NAME)     // mOriginatingPackageName
                    newSource.writeString(PLAY_STORE_PACKAGE_NAME)     // mInstallingPackageName
                    newSource.writeString(PLAY_STORE_PACKAGE_NAME)     // mUpdateOwnerPackageName
                    newSource.writeInt(2)                              // mPackageSource

                    newSource.setDataPosition(0)
                    return originalCreator.createFromParcel(newSource)
                }

                override fun newArray(size: Int): Array<out InstallSourceInfo?>? {
                    return originalCreator.newArray(size)
                }
            }
            try {
                findField(InstallSourceInfo::class.java, "CREATOR")?.set(null, creator)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun findField(clazz: Class<*>, fieldName: String): Field? {
            var currentClass: Class<*>? = clazz
            while (currentClass != null) {
                try {
                    val field = currentClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field
                } catch (e: NoSuchFieldException) {
                    currentClass = currentClass.superclass
                }
            }
            return null
        }
    }
}