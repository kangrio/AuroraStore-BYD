package com.aurora.store.patch.util

import android.content.Context
import android.util.Base64
import com.aurora.store.BuildConfig
import com.aurora.store.R
import com.aurora.store.patch.ConstantsPatch
import com.aurora.store.patch.state.PatchProgressState
import com.reandroid.apk.ApkModule
import com.reandroid.app.AndroidManifest
import com.reandroid.archive.ByteInputSource
import com.reandroid.arsc.chunk.xml.ResXmlElement
import com.reandroid.arsc.value.ValueType
import java.io.File
import java.io.InputStream

/**
 * Avoid modifying the DEX to reduce patching time.
 */
class Patcher(val context: Context, val packageName: String, val apkFiles: List<File>) {
    private val progressMessages = apkFiles.flatMap {
        listOf(
            "Patching ${it.nameWithoutExtension}",
            "Signing ${it.nameWithoutExtension}"
        )
    }
    var signOnly: Boolean = false

    fun start(): List<File> {
        val patchDir = File(apkFiles[0].parentFile, "patch").also { it.mkdirs() }
        val isPatched = patchDir.listFiles()?.isNotEmpty() == true && apkFiles.size == patchDir.listFiles()?.size
        if (isPatched) return patchDir.listFiles()!!.toList()

        progressState.start(packageName, progressMessages)
        val patchedApks = mutableListOf<File>()

        apkFiles.forEachIndexed { fileIndex, originalApk ->
            val outputFile = File(patchDir, originalApk.name)

            val patchedApk = patchSingle(fileIndex, originalApk)
            signApk(fileIndex, patchedApk, outputFile)
            patchedApks.add(outputFile)
        }

        progressState.finish()

        return patchedApks
    }

    fun startWithReplace() {
        val patchedApks = start()
        apkFiles.forEachIndexed { index, file ->
            file.delete()
            patchedApks[index].renameTo(file)
        }
    }

    fun isBaseModule(apkModule: ApkModule): Boolean {
        return apkModule.hasAndroidManifest()
                && !apkModule.getAndroidManifest().isSplit()
    }

    fun patchSingle(fileIndex: Int, originalApk: File): File {
        val patchStepIndex = fileIndex * 2
        progressState.beginStep(patchStepIndex, progressMessages[patchStepIndex])

        val apkModule = ApkModule.loadApkFile(originalApk)
        if (!isBaseModule(apkModule) || signOnly) {
            apkModule.destroy()
            progressState.completeStep(patchStepIndex)
            return originalApk
        }

        val patchedApk = File.createTempFile("patched_${System.currentTimeMillis()}", ".apk")

        try {
            patchAndroidManifest(apkModule, getSignatureBase64(apkModule))
            addPatchedDexToApk(apkModule)
            replaceMicroGProfiles(apkModule)

            apkModule.writeApk(patchedApk)
            apkModule.destroy()
        } catch (e: Exception) {
            progressState.failStep(patchStepIndex, e.message ?: "Failed on ${originalApk.name}")
        } finally {
            apkModule.destroy()
        }

        progressState.completeStep(patchStepIndex)
        return patchedApk
    }

    fun signApk(
        fileIndex: Int,
        apkFile: File,
        outputFile: File
    ): File {
        val patchStepIndex = fileIndex * 2 + 1
        progressState.beginStep(patchStepIndex, progressMessages[patchStepIndex])
        try {
            ApkSignerHelper.signApk(context, apkFile, outputFile)
        } catch (e: Exception) {
            progressState.failStep(patchStepIndex, e.message ?: "Failed on ${apkFile.name}")
        }
        progressState.completeStep(patchStepIndex)
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

    fun addPermission(apkModule: ApkModule, name: String) {
        val manifest = apkModule.androidManifest.manifestElement
        val permissionExist =
            manifest.getElements(AndroidManifest.TAG_uses_permission)?.asSequence()?.any {
                it.getOrCreateAndroidAttribute(
                    null,
                    android.R.attr.name
                ).valueAsString == name
            } == true
        if (permissionExist) return

        val position = manifest.lastIndexOf(AndroidManifest.TAG_uses_permission) + 1
        manifest.newElementAt(position, AndroidManifest.TAG_uses_permission).apply {
            createAndroidAttribute(null, android.R.attr.name).valueAsString = name
        }
    }

    private fun patchAndroidManifest(apkModule: ApkModule, signatureData: String) {
        /**
         * Fix missing GMS when package is installed.
         * See: https://github.com/kangrio/AuroraStore-BYD/issues/18.
         */
        addPermission(apkModule, "android.permission.QUERY_ALL_PACKAGES")

        val application: ResXmlElement = apkModule.androidManifest.applicationElement

        application.getOrCreateAndroidAttribute(
            "appComponentFactory", 0
        ).valueAsString = ConstantsPatch.PATCH_APP_COMPONENT_FACTORY_CLASS
        addMetaData(apkModule, ConstantsPatch.META_DATA_SPOOFED_CERTIFICATES, signatureData)
        addMetaData(apkModule, ConstantsPatch.META_DATA_PATCH_VERSION_CODE, BuildConfig.PATCH_VERSION_CODE, ValueType.DEC)

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

    fun replaceMicroGProfiles(apkModule: ApkModule) {
        if (apkModule.packageName != MICROG_PACKAGE_NAME) return

        val profileXmlBytes = context.resources
            .openRawResource(R.raw.profile_pixel_xl_10)
            .use { it.readBytes() }
        val profileXml = ByteInputSource(profileXmlBytes, "res/xml/profile_pixel_xl_10.xml")
        apkModule.add(profileXml)

        val tableBlock = apkModule.tableBlock
        val packageBlock = tableBlock.getPackageBlockById(0x7f)
        val xmlType = packageBlock.getOrCreateTypeBlock("", "xml")
        val entry = xmlType.getOrCreateEntry("profile_bullhead_27")
        entry.valueAsString = "res/xml/profile_pixel_xl_10.xml"
    }

    companion object {
        private const val MICROG_PACKAGE_NAME = "com.google.android.gms"
        private const val MICROG_SETTINGS_PROVIDER_AUTHORITY = "org.microg.gms.settings"

        val progressState = PatchProgressState()
    }
}