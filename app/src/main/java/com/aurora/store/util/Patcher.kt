package com.aurora.store.util
import android.content.Context
import android.util.Base64
import com.aurora.store.R
import com.reandroid.apk.ApkModule
import com.reandroid.app.AndroidManifest
import com.reandroid.archive.ByteInputSource
import com.reandroid.arsc.chunk.xml.ResXmlElement
import com.reandroid.arsc.value.ValueType
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

    fun addMetaData(apkModule: ApkModule, name: String, value: Any, valueType: ValueType = ValueType.STRING) {
        val application: ResXmlElement = apkModule.androidManifest.applicationElement

        application.newElement(AndroidManifest.TAG_meta_data).apply {
            createAndroidAttribute(null, android.R.attr.name).valueAsString = name
            when(valueType) {
                ValueType.BOOLEAN -> createAndroidAttribute(null, android.R.attr.value).valueAsBoolean = value as Boolean
                ValueType.DEC -> createAndroidAttribute(null, android.R.attr.value).setValueAsDecimal(value as Int)
                ValueType.HEX -> createAndroidAttribute(null, android.R.attr.value).setValueAsDecimal(value as Int)
                ValueType.FLOAT -> createAndroidAttribute(null, android.R.attr.value).valueAsFloat = value as Float
                ValueType.STRING -> createAndroidAttribute(null, android.R.attr.value).valueAsString = value.toString()
                ValueType.REFERENCE -> createAndroidAttribute(null, android.R.attr.value).valueAsResourceId = value as Int
                else -> createAndroidAttribute(null, android.R.attr.value).valueAsString = value.toString()
            }
        }
    }

    private fun patchAndroidManifest(apkModule: ApkModule, signatureData: String) {
        val application: ResXmlElement = apkModule.androidManifest.applicationElement

        application.getOrCreateAndroidAttribute(
            "appComponentFactory", 0
        ).valueAsString = "com.kangrio.extension.SpoofAppComponentFactory"
        addMetaData(apkModule, "org.microg.gms.spoofed_certificates", signatureData)

        // source https://github.com/microg/GmsCore/blob/master/play-services-core/src/huawei/AndroidManifest.xml
        if (apkModule.packageName == "com.google.android.gms") {
            addMetaData(apkModule, "org.microg.gms.settings.checkin_enable_service", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.gcm_enable_mcs_service", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.auth_manager_visible", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.auth_include_android_id", false, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.auth_strip_device_name", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.auth_two_step_verification", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.auth_allow_find_devices", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.droidguard_enabled", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.safetynet_enabled", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_billing", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_licensing_purchase_free_apps", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_licensing", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_asset_delivery", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_device_sync", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_split_install", false, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.game_allow_create_player", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.allow_upload_game_played", true, ValueType.BOOLEAN)
            addMetaData(apkModule, "org.microg.gms.settings.vending_apps_install", true, ValueType.BOOLEAN)
        }
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