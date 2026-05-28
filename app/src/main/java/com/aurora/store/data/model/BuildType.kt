package com.aurora.store.data.model

import com.aurora.Constants
import com.aurora.store.BuildConfig

/**
 * Class representing build types for Aurora Store
 */
enum class BuildType(var packageName: String) {
    RELEASE("com.aurora.store"),
    NIGHTLY("com.aurora.store.nightly"),
    DEBUG("com.aurora.store.debug");

    init {
        if (BuildConfig.FLAVOR == Constants.FLAVOUR_BYD) {
            packageName = packageName.replace("com.aurora.store", "com.aurora.store.byd")
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
