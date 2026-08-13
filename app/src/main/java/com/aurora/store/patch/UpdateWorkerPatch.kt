package com.aurora.store.patch

import android.content.Context
import android.util.Log
import com.aurora.Constants
import com.aurora.extensions.TAG
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.EncodedCertificateSet
import com.aurora.store.patch.apps.update.MicroGUpdate
import com.aurora.store.util.CertUtil
import com.aurora.store.util.PackageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateWorkerPatch {
    suspend fun getMicroGUpdate(context: Context): List<App>? {
        return withContext(Dispatchers.IO) {
            val updates = mutableListOf<App>()
            try {
                val microGBundle = listOf(
                    Constants.PACKAGE_NAME_GMS,
                    Constants.PACKAGE_NAME_PLAY_STORE
                ).filter {
                    CertUtilPatch.isSignedByAuroraStore(context, it)
                }

                if (microGBundle.isEmpty()) return@withContext null

                val microGLatestRelease = MicroGUpdate.getLatestRelease()
                microGBundle.forEach { pkg ->
                    val externalApk =
                        MicroGUpdate.getUpdate(pkg, microGLatestRelease) ?: return@forEach
                    val isCanUpdate = PackageUtil.isUpdatable(
                        context,
                        externalApk.packageName,
                        externalApk.versionCode
                    )

                    if (isCanUpdate) {
                        updates.add(
                            App(
                                packageName = externalApk.packageName,
                                versionCode = externalApk.versionCode,
                                versionName = externalApk.versionName,
                                changes = microGLatestRelease.body,
                                size = externalApk.fileList.first().size,
                                updatedOn = microGLatestRelease.published_at,
                                displayName = externalApk.displayName,
                                developerName = externalApk.developerName,
                                iconArtwork = Artwork(url = externalApk.iconURL),
                                fileList = externalApk.fileList,
                                isFree = true,
                                isInstalled = true,
                                certificateSetList = CertUtil.getEncodedCertificateHashes(
                                    context,
                                    externalApk.packageName
                                ).map {
                                    EncodedCertificateSet(certificateSet = it, sha256 = String())
                                }.toMutableList()
                            )
                        )
                    }
                }
                if (updates.isNotEmpty()) {
                    return@withContext updates
                }

            } catch (exception: Exception) {
                Log.e(TAG, "Failed to check microg-updates", exception)
                return@withContext null
            }

            Log.i(TAG, "No MicroG-updates found!")
            return@withContext null
        }
    }
}