package com.aurora.store.patch

import com.aurora.gplayapi.data.serializers.LocaleSerializer
import com.aurora.gplayapi.data.serializers.PropertiesSerializer
import com.aurora.store.BuildConfig
import com.aurora.store.patch.data.GithubRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.OkHttpClient
import okhttp3.Request

object UtilPatch {
    fun isBydFLAVOUR(): Boolean {
        return BuildConfig.FLAVOR == ConstantsPatch.FLAVOUR_BYD
    }

    suspend fun getGithubRepoUrl(): String? = withContext(Dispatchers.IO) {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            serializersModule = SerializersModule {
                contextual(LocaleSerializer)
                contextual(PropertiesSerializer)
            }
            explicitNulls = false
        }


        val client = OkHttpClient()
        try {
            val request = Request.Builder()
                .url(ConstantsPatch.GITHUB_API_REPO_URL)
                .addHeader("Accept", "*/*")
                .addHeader("User-Agent", "request")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body.string()

            val repoUrl = json.decodeFromString<GithubRepo>(body).html_url

            "${repoUrl}/releases/latest/download/release_feed.json"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            client.dispatcher.executorService.shutdown()
        }
    }
}