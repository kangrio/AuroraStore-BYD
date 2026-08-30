package com.kangrio.extension.hook

import com.kangrio.extension.hook.base.AbstractHook
import de.robv.android.xposed.XC_MethodHook

class Hook1 : AbstractHook() {
    override val TAG: String = "InstallerHook"
    override fun init() {
        getInstallerPackageNameHook()
    }

    private fun getInstallerPackageNameHook() {
        val hook: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                param.setResult("com.android.vending")
            }
        }

        hookAllMethods("android.app.ApplicationPackageManager", "getInstallerPackageName", hook)
        hookAllMethods("android.content.pm.InstallSourceInfo", "getInstallingPackageName", hook)
    }
}