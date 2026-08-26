package com.kangrio.extension.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement

object PairipHook : AbstractHook() {
    override val TAG: String = "PairipHook"

    fun init() {
        pairipHook()
    }

    private fun pairipHook() {
        val processResponseHook: XC_MethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.args[0] = 0
            }
        }

        hookAllMethods("com.pairip.licensecheck.ResponseValidator", "validateResponse", XC_MethodReplacement.DO_NOTHING)
        hookAllMethods("com.pairip.licensecheck.LicenseClient", "processResponse", processResponseHook)

        hookAllMethods("com.pairip.licensecheck3.ResponseValidator", "validateResponse", XC_MethodReplacement.DO_NOTHING)
        hookAllMethods("com.pairip.licensecheck3.LicenseClientV3", "processResponse", processResponseHook)
    }
}