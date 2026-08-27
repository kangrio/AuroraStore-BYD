package com.kangrio.extension

import android.content.pm.InstallSourceInfo
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.lang.reflect.Field
import java.lang.reflect.Proxy

class SpoofInstallerSource {
    companion object {
        fun init() {
            getInstallerPackageName()
            installSourceInfo()
        }

        fun getInstallerPackageName() {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val iPackageManagerClass = Class.forName("android.content.pm.IPackageManager")
            val field = activityThreadClass
                .getDeclaredField("sPackageManager")
                .apply {
                    isAccessible = true
                }


            val original = iPackageManagerClass.cast(field.get(null))

            val proxy = Proxy.newProxyInstance(
                iPackageManagerClass.classLoader,
                arrayOf(iPackageManagerClass)
            ) { _, method, args ->

                if (method.name.equals("getInstallerPackageName")) {
                    "com.android.vending"
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

                    val newSource = Parcel.obtain()
                    newSource.writeString("com.android.vending")
                    newSource.writeParcelable(null, 0)
                    newSource.writeString("com.android.vending")
                    newSource.writeString("com.android.vending")
                    newSource.writeString("com.android.vending")
                    newSource.writeInt(2)
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