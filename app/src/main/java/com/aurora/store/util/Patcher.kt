package com.aurora.store.util
import android.content.Context
import android.util.Base64
import com.aurora.store.R
import com.reandroid.apk.ApkModule
import com.reandroid.app.AndroidManifest
import com.reandroid.archive.ByteInputSource
import com.reandroid.arsc.chunk.xml.ResXmlElement
import java.io.File
import java.io.InputStream

class Patcher(val context: Context) {
    fun patch(
        apkFiles: List<File>
    ): List<File> {
        val patchDir = File(apkFiles[0].parentFile, "patch").also { it.mkdirs() }
        val patchedApks = mutableListOf<File>()

        apkFiles.forEach { originalApk ->
            val patchedApk = patchSingle(originalApk)
            val outputFile = File(patchDir, originalApk.name)
            patchedApks.add(outputFile)
            try {
                patchedApk.inputStream().use { fileIn ->
                    fileIn.copyTo(outputFile.outputStream())
                }
            } finally {
                patchedApk.delete()
            }
        }

        return patchedApks
    }

    fun patchWithReplace(
        apkFiles: List<File>
    ) {
        val patchedApks = patch(apkFiles)
        apkFiles.forEachIndexed { index, file ->
            file.delete()
            patchedApks[index].renameTo(file)
        }
    }

    fun patchSingle(originalApk: File): File {
        val apkModule = ApkModule.loadApkFile(originalApk)
        val patchedApk = File.createTempFile("patched_${System.currentTimeMillis()}", ".apk")

        if (apkModule.isBaseModule) {
            patchAndroidManifest(apkModule, getSignatureBase64(apkModule))
            addPatchedDexToApk(apkModule)

            val tmpApk = File.createTempFile("tmp_${System.currentTimeMillis()}", ".apk")
            apkModule.writeApk(tmpApk)

            signApk(tmpApk, patchedApk)
            tmpApk.delete()
        } else {
            signApk(originalApk, patchedApk)
        }

        apkModule.destroy()

        return patchedApk
    }

    fun signApk(
        apkFile: File,
        outputFile: File
    ): File {
        ApkSignerHelper.signApk(context, apkFile, outputFile)
        return outputFile
    }

    private fun getSignatureBase64(apkModule: ApkModule): String {
        for (signature in apkModule.apkSignatureBlock) {
            if (signature.id.name() in listOf("v1", "V2", "V3", "V31")) {
                val base64Signature = Base64.encodeToString(
                    signature.certificates.next().certificateBytes,
                    Base64.DEFAULT
                )
                return base64Signature
            }
        }
        return ""
    }

    private fun patchAndroidManifest(apkModule: ApkModule, signatureData: String) {
        val application: ResXmlElement = apkModule.androidManifest.applicationElement

        val appComponentFactory = application.getOrCreateAndroidAttribute("appComponentFactory", 0)
        appComponentFactory.valueAsString = "com.kangrio.extension.AppFactory"
        val meta: ResXmlElement = application.newElement(AndroidManifest.TAG_meta_data)
        val name = meta.createAndroidAttribute(
            AndroidManifest.NAME_name, AndroidManifest.ID_name
        )
        name.valueAsString = "org.microg.gms.spoofed_certificates"
        val value = meta.createAndroidAttribute(
            AndroidManifest.NAME_value, AndroidManifest.ID_value
        )
        value.valueAsString = signatureData
    }

    fun addPatchedDexToApk(apkModule: ApkModule) {
        val dexInputStream: InputStream = context.resources.openRawResource(R.raw.classes)
        val dexBytes = dexInputStream.readBytes()
        dexInputStream.close()

        val classesDexName = "classes${
            apkModule.listDexFiles()
                .filter { it.name.startsWith("classes") && !it.name.contains("/") }.size + 1
        }.dex"
        val classesDex = ByteInputSource(dexBytes, classesDexName)
        apkModule.add(classesDex)
    }
}