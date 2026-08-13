package com.aurora.store.patch

object ConstantsPatch {
    const val FLAVOUR_BYD = "byd"
    const val FLAVOUR_BYD_PACKAGE_NAME = "com.aurora.store.byd"
    const val GITHUB_API_URL = "https://api.github.com/repositories"
    const val GITHUB_SELF_REPO_ID = "1228674301"
    const val GITHUB_API_REPO_URL = "${GITHUB_API_URL}/${GITHUB_SELF_REPO_ID}"
    const val GITHUB_APPS_REPO_ID = "1301975707"
    const val UPDATE_APPS_URL = "${GITHUB_API_URL}/${GITHUB_APPS_REPO_ID}/releases/latest"

    const val PATCH_APP_COMPONENT_FACTORY_CLASS = "com.kangrio.extension.SpoofAppComponentFactory"
    const val META_DATA_SPOOFED_CERTIFICATES = "org.microg.gms.spoofed_certificates"
    const val META_DATA_PATCH_VERSION_CODE = "com.kangrio.extension.patch_version_code"
}