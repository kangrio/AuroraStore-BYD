package com.aurora.store.patch

import com.aurora.store.BuildConfig

object UtilPatch {
    fun isBydFLAVOUR(): Boolean {
        return BuildConfig.FLAVOR == ConstantsPatch.FLAVOUR_BYD
    }
}