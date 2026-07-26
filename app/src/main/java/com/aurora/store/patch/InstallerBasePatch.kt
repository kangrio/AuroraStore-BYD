package com.aurora.store.patch

import android.content.Context
import com.aurora.store.patch.apps.ExternalAppsPatch
import com.aurora.store.patch.util.ApkSignerHelper
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
        if (packageName in ExternalAppsPatch.apps) {
            val needSign = !PackageUtil.isInstalled(context, packageName) || CertUtilPatch.isSignedByAuroraStore(context, packageName)
            if (!needSign) return files

            return files.mapNotNull { file ->
                runCatching {
                    ApkSignerHelper.signApk(context, file, file)
                    file
                }.getOrNull()
            }
        }

        val patcher = Patcher(context)
        return patcher.patch(files)
    }
}