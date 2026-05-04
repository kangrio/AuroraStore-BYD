package com.aurora.store.data.model

import com.aurora.Constants.PACKAGE_NAME_GMS
import com.aurora.Constants.PACKAGE_NAME_PLAY_STORE
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.store.data.room.suite.ExternalApk
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MicroGUpdate {
    companion object {
        private const val ICON_BASE_URL = "https://raw.githubusercontent.com/microg"
        private const val ICON_FILE_PATH = "src/main/res/mipmap-xxxhdpi/ic_app.png"

        fun getLatestRelease(): GitHubRelease {
            val client = OkHttpClient()

            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/microg/GmsCore/releases/latest")
//                    .url("https://api.github.com/repos/microg/GmsCore/releases/283774774")
                    .addHeader("Accept", "*/*")
                    .addHeader("User-Agent", "request")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body.string()

                val json = JSONObject(body)

                val assetsJson = json.getJSONArray("assets")
                val assets = mutableListOf<Asset>()

                for (i in 0 until assetsJson.length()) {
                    val a = assetsJson.getJSONObject(i)
                    assets.add(
                        Asset(
                            name = a.getString("name"),
                            browser_download_url = a.getString("browser_download_url"),
                            size = a.getLong("size"),
                            sha256 = a.getString("digest").removePrefix("sha256:")
                        )
                    )
                }

                return GitHubRelease(
                    tag_name = json.getString("tag_name"),
                    name = json.getString("name"),
                    body = json.getString("body"),
                    assets = assets,
                    published_at = json.getString("published_at")
                )

            } catch (e: Exception) {
                e.printStackTrace()
                return GitHubRelease("0", "0", "", emptyList(), "")
            } finally {
                client.dispatcher.executorService.shutdown()
            }
        }

        fun getMicroGApk(githubRelease: GitHubRelease) : ExternalApk {
            val latestGms = githubRelease.assets
                .filter { it.name.contains(PACKAGE_NAME_GMS) }
                .filter { it.name.endsWith(".apk") }
                .first { !it.name.contains("hw") }


            return ExternalApk(
                packageName = PACKAGE_NAME_GMS,
                versionCode = latestGms.name.removeSuffix(".apk").split("-").last().toLong(),
                versionName = githubRelease.tag_name,
                displayName = "microG Services",
                iconURL = "$ICON_BASE_URL/GmsCore/refs/heads/master/play-services-core/$ICON_FILE_PATH",
                developerName = "microG Team",
                fileList =  listOf(
                    PlayFile(
                        url = latestGms.browser_download_url,
                        name = latestGms.name,
                        size = latestGms.size,
                        sha256 = latestGms.sha256
                    )
                )
            )
        }

        fun getCompanionApk(githubRelease: GitHubRelease) : ExternalApk {
            val latestCompanion = githubRelease.assets
                .filter { it.name.contains(PACKAGE_NAME_PLAY_STORE) }
                .filter { it.name.endsWith(".apk") }
                .first { !it.name.contains("hw") }


            return ExternalApk(
                packageName = PACKAGE_NAME_PLAY_STORE,
                versionCode = latestCompanion.name.removeSuffix(".apk").split("-").last().toLong(),
                versionName = githubRelease.tag_name,
                displayName = "microG Companion",
                iconURL = "$ICON_BASE_URL/GmsCore/refs/heads/master/vending-app/$ICON_FILE_PATH",
                developerName = "microG Team",
                fileList =  listOf(
                    PlayFile(
                        url = latestCompanion.browser_download_url,
                        name = latestCompanion.name,
                        size = latestCompanion.size,
                        sha256 = latestCompanion.sha256
                    )
                )
            )
        }
    }
}

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<Asset>,
    val published_at: String
)

data class Asset(
    val name: String,
    val browser_download_url: String,
    val size: Long,
    val sha256: String
)