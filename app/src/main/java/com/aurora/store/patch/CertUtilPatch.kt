package com.aurora.store.patch

import android.content.Context
import android.util.Log
import com.aurora.extensions.TAG
import com.aurora.store.BuildConfig
import com.aurora.store.patch.util.ApkSignerHelper
import com.aurora.store.util.CertUtil

object CertUtilPatch {
    fun isSignedByAuroraStore(context: Context, packageName: String): Boolean = try {
        if (BuildConfig.FLAVOR != ConstantsPatch.FLAVOUR_BYD) return false

        CertUtil.getX509Certificates(context, packageName).any { cert ->
            val auroraStoreCert = ApkSignerHelper.getCertificate(context)

            // equal
            cert.publicKey == auroraStoreCert.publicKey
        }
    } catch (exception: Throwable) {
        Log.e(TAG, "Failed to check signing cert for $packageName", exception)
        false
    }
}