package com.aurora.store.util

import android.content.Context
import com.aurora.Constants

object FlavouredUtil : IFlavouredUtil {

    override val defaultDispensers = setOf(Constants.URL_DISPENSER)

    override fun promptMicroGInstall(context: Context): Boolean {
        return !PackageUtil.isInstalled(context, Constants.PACKAGE_NAME_GMS) || !PackageUtil.isInstalled(context, Constants.PACKAGE_NAME_PLAY_STORE)
    }
}