package com.aurora.store.patch

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AuroraAppPatch {
    fun setupCrashHandler(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        val crashFile = File(context.getExternalFilesDir("log"), "crash.txt").apply {
            parentFile?.mkdirs()
            createNewFile()
        }
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(crashFile, throwable.stackTraceToString())
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashLog(file: File, text: String) {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)
        val logText = "$dateTime\n$text\n===================================\n"
        file.appendText(logText)
    }
}