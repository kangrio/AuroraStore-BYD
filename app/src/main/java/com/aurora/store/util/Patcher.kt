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
        if (apkModule.packageName == MICROG_PACKAGE_NAME) {
            applyMicroGSettings(apkModule)
        }
    }

    fun applyMicroGSettings(apkModule: ApkModule) {
        addMicroGSettings(apkModule, "device_profile", "bullhead_27", ValueType.STRING)

        addMicroGSettings(apkModule, "checkin_enable_service", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "gcm_enable_mcs_service", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "auth_manager_visible", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "auth_include_android_id", false, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "auth_strip_device_name", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "auth_two_step_verification", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "auth_allow_find_devices", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "droidguard_enabled", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "safetynet_enabled", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_billing", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_licensing_purchase_free_apps", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_licensing", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_asset_delivery", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_device_sync", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_split_install", false, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "game_allow_create_player", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "allow_upload_game_played", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "vending_apps_install", true, ValueType.BOOLEAN)

        /**  location settings
         * Row: 0
         * location_wifi_mls=1,
         * location_wifi_moving=0,
         * location_wifi_learning=1,
         * location_wifi_caching=1,
         * location_cell_mls=1,
         * location_cell_learning=1,
         * location_cell_caching=1,
         * location_geocoder_nominatim=1,
         * location_ichnaea_endpoint=NULL,
         * location_online_source=positon,
         * location_ichnaea_contribute=0
         */
        addMicroGSettings(apkModule, "location_wifi_mls", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_wifi_moving", false, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_wifi_learning", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_wifi_caching", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_cell_mls", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_cell_learning", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_cell_caching", true, ValueType.BOOLEAN)
        addMicroGSettings(apkModule, "location_geocoder_nominatim", true, ValueType.BOOLEAN)
//            addMicroGSettings(apkModule, "location_ichnaea_endpoint", "", ValueType.STRING)
        addMicroGSettings(apkModule, "location_online_source", "position", ValueType.STRING)
        addMicroGSettings(apkModule, "location_ichnaea_contribute", false, ValueType.BOOLEAN)
    }

    fun addMicroGSettings(apkModule: ApkModule, name: String, value: Any, valueType: ValueType) {
        addMetaData(apkModule, "$MICROG_SETTINGS_PROVIDER_AUTHORITY.$name", value, valueType)
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

    companion object {
        const val MICROG_PACKAGE_NAME = "com.google.android.gms"
        const val MICROG_SETTINGS_PROVIDER_AUTHORITY = "org.microg.gms.settings"
    }
}