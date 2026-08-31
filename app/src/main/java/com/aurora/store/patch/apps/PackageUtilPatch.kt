package com.aurora.store.patch.apps

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.aurora.store.BuildConfig
import com.aurora.store.data.installer.AppInstaller
import com.aurora.store.data.model.Installer
import com.aurora.store.patch.ConstantsPatch
import com.aurora.store.util.PackageUtil

object PackageUtilPatch {
    fun canRequestPackageInstalls(context: Context): Boolean {
        val currentInstaller = AppInstaller.getCurrentInstaller(context)
        return when (currentInstaller) {
            Installer.ROOT -> AppInstaller.hasRootAccess()
            Installer.AM -> AppInstaller.hasAppManager(context)
            Installer.SHIZUKU -> AppInstaller.hasShizukuOrSui(context) && AppInstaller.hasShizukuPerm()
            else -> false
        }
    }

    fun getPatchVersionCode(context: Context, packageName: String): Int {
        try {
            val packageInfo = PackageUtil.getPackageInfo(context, packageName, PackageManager.GET_META_DATA)
            return packageInfo.applicationInfo?.metaData?.getInt(ConstantsPatch.META_DATA_PATCH_VERSION_CODE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        } catch (_: Throwable) {
            return Int.MIN_VALUE
        }
    }

    fun isHasNewerPatch(context: Context, packageName: String): Boolean {
        return BuildConfig.PATCH_VERSION_CODE > getPatchVersionCode(context, packageName)
    }

    fun isUpdatable(context: Context, packageName: String, versionCode: Long): Boolean {
        return try {
            // original apps
            if (packageName !in ExternalAppsPatch.apps.keys || !isMorphePatch(context, packageName)) {
                try {
                    if (isHasNewerPatch(context, packageName)) {
                        return true
                    }

                    val packageInfo = PackageUtil.getPackageInfo(context, packageName)
                    return versionCode > PackageInfoCompat.getLongVersionCode(packageInfo)
                } catch (_: PackageManager.NameNotFoundException) {
                    return false
                }
            }

            // fallback
            // prebuild apk patched with morphe-patches
            val currentVersion = morpheVersion(context, packageName)
            val latestVersion = ExternalAppsPatch.getLatestRelease().tag_name.substring(1).replace(".", "").toInt()
            return latestVersion > currentVersion
        } catch (_: Throwable) {
            false
        }
    }

    fun morpheVersion(context: Context, packageName: String): Int {
        try {
            val packageInfo = PackageUtil.getPackageInfo(context, packageName, PackageManager.GET_META_DATA)
            return packageInfo.applicationInfo?.metaData?.getInt("morphe_version", Int.MAX_VALUE) ?: Int.MAX_VALUE
        } catch (_: Throwable) {
            return Int.MAX_VALUE
        }
    }

    fun isMorphePatch(context: Context, packageName: String): Boolean {
        return try {
            val packageInfo = PackageUtil.getPackageInfo(context, packageName, PackageManager.GET_META_DATA)
            packageInfo.applicationInfo?.metaData?.containsKey("morphe_version") == true
        } catch (_: Throwable) {
            false
        }
    }
}