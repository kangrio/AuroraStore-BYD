package com.aurora.store.patch.apps

import android.content.Context
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.data.serializers.LocaleSerializer
import com.aurora.gplayapi.data.serializers.PropertiesSerializer
import com.aurora.store.patch.data.GitHubRelease
import com.aurora.store.data.room.download.Download
import com.aurora.store.patch.ConstantsPatch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors

object ExternalAppsPatch {
    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val YOUTUBE_MUSIC_PACKAGE = "com.google.android.apps.youtube.music"

    val apps = mapOf(
        YOUTUBE_PACKAGE to "Youtube",
        YOUTUBE_MUSIC_PACKAGE to "YTMusic"
    )

    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        serializersModule = SerializersModule {
            contextual(LocaleSerializer)
            contextual(PropertiesSerializer)
        }
        explicitNulls = false
    }

    fun getLatestRelease(): GitHubRelease {
        return Executors.newSingleThreadExecutor().submit<GitHubRelease> {
            val client = OkHttpClient()

            try {
                val request = Request.Builder()
                    .url(ConstantsPatch.UPDATE_APPS_URL)
                    .addHeader("Accept", "*/*")
                    .addHeader("User-Agent", "request")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body.string()

                json.decodeFromString<GitHubRelease>(body)
            } catch (e: Exception) {
                e.printStackTrace()
                GitHubRelease("0", "0", "", emptyList(), "")
            } finally {
                client.dispatcher.executorService.shutdown()
            }
        }.get()
    }

    fun get(context: Context, download: Download): List<PlayFile> {
        if (!PackageUtilPatch.isMorphePatch(context, download.packageName)) return download.fileList

        val name = apps[download.packageName] ?: return download.fileList
        val githubRelease = getLatestRelease()
        val latestApp = githubRelease.assets
            .firstOrNull { it.name.startsWith(name) } ?: return download.fileList

        return listOf(
            PlayFile(
                url = latestApp.browser_download_url,
                name = latestApp.name,
                size = latestApp.size,
                sha256 = latestApp.sha256
            )
        )
    }
}