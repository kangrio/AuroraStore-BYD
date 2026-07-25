package com.aurora.store.patch

import android.content.Context
import android.util.Log
import com.aurora.Constants
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.EncodedCertificateSet
import com.aurora.store.patch.apps.update.MicroGUpdate
import com.aurora.store.util.CertUtil
import com.aurora.store.util.PackageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateWorkerPatch {
    const val TAG = "UpdateWorkerPatch"
    suspend fun getMicroGUpdate(context: Context): List<App>? {
        return withContext(Dispatchers.IO) {
            val updates = mutableListOf<App>()
            try {
                val microGLatestRelease = MicroGUpdate.getLatestRelease()
                val microg = MicroGUpdate.getMicroGApk(microGLatestRelease)
                val companion = MicroGUpdate.getCompanionApk(microGLatestRelease)

                val isMicroGUpdate =
                    PackageUtil.isUpdatable(context, Constants.PACKAGE_NAME_GMS, microg.versionCode)

                val isCompanionUpdate =
                    PackageUtil.isUpdatable(context, Constants.PACKAGE_NAME_PLAY_STORE, companion.versionCode)

                if (isMicroGUpdate) {
                    updates.add(
                        App(
                            packageName = microg.packageName,
                            versionCode = microg.versionCode,
                            versionName = microg.versionName,
                            changes = microGLatestRelease.body,
                            size = microg.fileList.first().size,
                            updatedOn = microGLatestRelease.published_at,
                            displayName = microg.displayName,
                            developerName = microg.developerName,
                            iconArtwork = Artwork(url = microg.iconURL),
                            fileList = microg.fileList,
                            isFree = true,
                            isInstalled = true,
                            certificateSetList = CertUtil.getEncodedCertificateHashes(
                                context,
                                microg.packageName
                            ).map {
                                EncodedCertificateSet(certificateSet = it, sha256 = String())
                            }.toMutableList()
                        )
                    )
                }

                if (isCompanionUpdate) {
                    updates.add(
                        App(
                            packageName = companion.packageName,
                            versionCode = companion.versionCode,
                            versionName = companion.versionName,
                            changes = microGLatestRelease.body,
                            size = companion.fileList.first().size,
                            updatedOn = microGLatestRelease.published_at,
                            displayName = companion.displayName,
                            developerName = companion.developerName,
                            iconArtwork = Artwork(url = companion.iconURL),
                            fileList = companion.fileList,
                            isFree = true,
                            isInstalled = true,
                            certificateSetList = CertUtil.getEncodedCertificateHashes(
                                context,
                                companion.packageName
                            ).map {
                                EncodedCertificateSet(certificateSet = it, sha256 = String())
                            }.toMutableList()
                        )
                    )
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