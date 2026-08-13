package com.aurora.store.patch

import android.content.Context
import com.aurora.store.patch.apps.ExternalAppsPatch
import com.aurora.store.patch.util.Patcher
import com.aurora.store.util.PackageUtil
import java.io.File

object InstallerBasePatch {
    fun getFiles(
        context: Context,
        packageName: String,
        files: List<File>
    ): List<File> {
        if (packageName == context.packageName) return files
        val patcher = Patcher(context, packageName, files)

        if (packageName in ExternalAppsPatch.apps) {
            val needSign = !PackageUtil.isInstalled(context, packageName) || CertUtilPatch.isSignedByAuroraStore(context, packageName)
            if (!needSign) return files

            patcher.signOnly = true
            return patcher.start()
        }

        return patcher.start()
    }
}