package com.kangrio.extension.hook

import com.kangrio.extension.hook.base.AbstractHook
import de.robv.android.xposed.XC_MethodHook
import java.io.File
import java.io.IOException

class Hook3 : AbstractHook() {
    override val TAG = "BypassRoot"

    override fun init() {
        rootBypassHook()
    }

    private fun rootBypassHook() {
        val existsHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val objects = param.thisObject as File
                if (isFileRootAccessRelated(objects.absolutePath)) {
                    param.result = false
                }
            }
        }

        val execHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val command: String = try {
                    param.args[0] as String
                } catch (e: ClassCastException) {
                    val firstArg = param.args[0] as Array<*>
                    firstArg.joinToString(" ") { it.toString() }
                }

                log("App tried to run Exec command  :: $command")

                if (command.contains("su") || command.contains("magisk") || command.contains("busybox")) {
                    param.result = null
                }

                if (command.contains("getprop") || command.contains("mount")) {
                    throw IOException("IO Error occurred")
                }
            }
        }

        hookAllMethods(File::class.java, "exists", existsHook)
        hookAllMethods(Runtime::class.java, "exec", execHook)
    }

    fun filePathEqualsOrEndsWith(filePath: String, pattern: String): Boolean {
        return filePath.contentEquals(pattern) || filePath.endsWith(pattern);
    }

    private fun isFileRootAccessRelated(filePath: String): Boolean {
        var result = false
        for (file in rootRelatedFiles) {
            if (filePathEqualsOrEndsWith(filePath, file)) {
                result = true
                break
            }
        }
        return result
    }

    val rootRelatedFiles = arrayOf(
        "magisk",
        "su",
        "busybox",
        "Superuser",
        "SuperSU",
        "daemonsu",
        "/data/adb/.boot_count"
    )
}