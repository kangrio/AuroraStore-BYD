package com.aurora.store.util

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.aurora.Constants
import com.aurora.store.patch.ConstantsPatch

object FlavouredUtil : IFlavouredUtil {

    override val defaultDispensers = setOf(Constants.URL_DISPENSER)

    override fun promptMicroGInstall(context: Context): Boolean {
        if (!Preferences.getBoolean(context, Preferences.PREFERENCE_INTRO)) {
            return false
        }
        val microGBundleInstalled  = isInstalledMicroGBundle(context)

        if (microGBundleInstalled ) {
            val gmsSupportsSpoofing = isCompatible(context, Constants.PACKAGE_NAME_GMS)
            val playStoreSupportsSpoofing = isCompatible(context, Constants.PACKAGE_NAME_PLAY_STORE)
            val isMicroGCompatible = gmsSupportsSpoofing && playStoreSupportsSpoofing

            if (!isMicroGCompatible) {
                val incompatibleComponents = buildList {
                    if (!gmsSupportsSpoofing) add("MicroG Service")
                    if (!playStoreSupportsSpoofing) add("MicroG Companion")
                }.joinToString(" and ")
                showToast(context, "$incompatibleComponents does not support spoofed certificates. Please uninstall it and install the compatible version from this AuroraStore.")
            }
        }

        return !microGBundleInstalled
    }

    private fun isInstalledMicroGBundle(context: Context): Boolean {
        return PackageUtil.isInstalled(context, Constants.PACKAGE_NAME_GMS) &&
                PackageUtil.isInstalled(context, Constants.PACKAGE_NAME_PLAY_STORE)
    }

    private fun isCompatible(context: Context, packageName: String): Boolean {
        if (!PackageUtil.isInstalled(context, packageName)) return false

        val supportsSpoofing = PackageUtil.getPackageInfo(context, packageName, PackageManager.GET_META_DATA)
            .applicationInfo?.metaData
            ?.containsKey(ConstantsPatch.META_DATA_SPOOFED_CERTIFICATES) == true

        return supportsSpoofing
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}