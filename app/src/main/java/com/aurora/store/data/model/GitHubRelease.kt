package com.aurora.store.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tag_name: String = String(),
    @SerialName("name") val name: String = String(),
    @SerialName("body") val body: String = String(),
    @SerialName("assets") val assets: List<Asset> = emptyList(),
    @SerialName("published_at") val published_at: String = String()
)

@Serializable
data class Asset(
    @SerialName("name") val name: String = String(),
    @SerialName("browser_download_url") val browser_download_url: String = String(),
    @SerialName("size") val size: Long = 0,
    @SerialName("digest") var digest: String = String()
) {
    val sha256: String
        get() = digest.removePrefix("sha256:")
}