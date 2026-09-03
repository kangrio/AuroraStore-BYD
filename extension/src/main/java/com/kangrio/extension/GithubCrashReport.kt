package com.kangrio.extension

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object GithubCrashReport {
    private const val TAG = "GithubCrashReport"
    private const val ENDPOINT_URL = "https://aurorabyd.pythonanywhere.com/crashreport"
    private const val CONNECT_TIMEOUT_MS = 8_000
    private const val READ_TIMEOUT_MS = 8_000
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var initialized = false
    @Volatile
    private var crashHandler: Thread.UncaughtExceptionHandler? = null
    @Volatile
    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    fun isEnable(context: Context): Boolean {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData?.getBoolean("com.kangrio.extensions.enable_crash_report") == true
    }

    fun init(context: Context) {
        if (!isEnable(context)) return

        if (initialized) return
        try {
            val appContext = context.applicationContext
            originalHandler = Thread.getDefaultUncaughtExceptionHandler()

            crashHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
                try {
                    val payload = buildPayload(appContext, throwable)
                    sendSync(payload)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send crash report", e)
                } finally {
                    originalHandler?.uncaughtException(thread, throwable)
                }
            }

            Thread.setDefaultUncaughtExceptionHandler(crashHandler)
            initialized = true

            Thread {
                while (initialized) {
                    try {
                        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()

                        if (currentHandler !== crashHandler) {
                            originalHandler = currentHandler
                            Thread.setDefaultUncaughtExceptionHandler(crashHandler)
                        }
                    } catch (_: Throwable) {
                    }

                    try {
                        Thread.sleep(1000)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }.apply {
                isDaemon = true
                start()
            }

        } catch (_: Throwable) {
        }
    }

    fun report(context: Context, throwable: Throwable) {
        val appContext = context.applicationContext
        executor.execute {
            try {
                val payload = buildPayload(appContext, throwable)
                sendSync(payload)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send crash report", e)
            }
        }
    }

    private fun buildPayload(context: Context, throwable: Throwable): JSONObject {
        val packageName = context.packageName
        var versionCode = 0L
        var versionName = "unknown"

        try {
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            versionName = pInfo.versionName ?: "unknown"
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read package info", e)
        }

        val stackTrace = throwable.stackTraceToString()

        return JSONObject().apply {
            put("android", Build.VERSION.RELEASE ?: "unknown")
            put("buildfingerprint", Build.FINGERPRINT ?: "unknown")
            put("package", packageName)
            put("versioncode", versionCode)
            put("versionname", versionName)
            put("report", stackTrace)
        }
    }

    private fun sendSync(payload: JSONObject) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(ENDPOINT_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val bodyBytes = payload.toString().toByteArray(Charsets.UTF_8)

            connection.outputStream.use { os: OutputStream ->
                os.write(bodyBytes)
                os.flush()
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                Log.w(TAG, "Crash report rejected: $code $responseText")
            } else {
                Log.d(TAG, "Crash report sent: $responseText")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Crash report send failed", e)
        } finally {
            connection?.disconnect()
        }
    }
}