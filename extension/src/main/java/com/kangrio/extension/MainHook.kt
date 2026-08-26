package com.kangrio.extension

import com.kangrio.extension.hook.BypassRootHook
import com.kangrio.extension.hook.InstallerHook
import com.kangrio.extension.hook.PairipHook
import inc.whew.android.fakegapps.FakeSignatures

object MainHook {
    fun init() {
        BypassRootHook.init()
        FakeSignatures.init()
        InstallerHook.init()
        PairipHook.init()
    }
}