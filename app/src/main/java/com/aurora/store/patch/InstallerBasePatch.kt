package com.aurora.store.patch

import android.content.Context
import com.aurora.store.patch.apps.ExternalAppsPatch
import com.aurora.store.patch.util.ApkSignerHelper
import com.aurora.store.patch.util.Patcher
import java.io.File

object InstallerBasePatch {
    fun getFiles(
        context: Context,
        packageName: String,
        files: List<File>
    ): List<File> {
        if (packageName == context.packageName) return files
        if (packageName in ExternalAppsPatch.apps) {
            val isSignedByAuroraStore = CertUtilPatch.isSignedByAuroraStore(context, packageName)
            if (!isSignedByAuroraStore) return files

            return files.map { file ->
                val parent = file.parentFile!!
                val tmp = File.createTempFile("tmp_", ".apk", parent)
                val signed = File.createTempFile("signed_", ".apk", parent)

                try {
                    file.copyTo(tmp, overwrite = true)
                    ApkSignerHelper.signApk(context, tmp, signed)
                    signed.copyTo(file, overwrite = true)
                    file
                } finally {
                    tmp.delete()
                    signed.delete()
                }
            }
        }

        val patcher = Patcher(context)
        return patcher.patch(files)
    }
}