package com.aurora.store.data.model

import com.aurora.store.BuildConfig
import com.aurora.store.patch.ConstantsPatch
import com.aurora.store.patch.UtilPatch

/**
 * Class representing build types for Aurora Store
 */
enum class BuildType(var packageName: String) {
    RELEASE("com.aurora.store"),
    NIGHTLY("com.aurora.store.nightly"),
    DEBUG("com.aurora.store.debug");

    init {
        if (UtilPatch.isBydFlavour()) {
            packageName = packageName.replace("com.aurora.store", ConstantsPatch.FLAVOUR_BYD_PACKAGE_NAME)
        }
    }

    companion object {

        /**
         * Returns current build type
         */
        @Suppress("KotlinConstantConditions")
        val CURRENT: BuildType
            get() = when (BuildConfig.BUILD_TYPE) {
                "release" -> RELEASE
                "nightly" -> NIGHTLY
                else -> DEBUG
            }

        /**
         * Returns package names for all possible build types
         */
        val PACKAGE_NAMES: List<String>
            get() = BuildType.entries.map { it.packageName }
    }
}
