package com.kangrio.extension.hook

import com.kangrio.extension.hook.base.AbstractHook
import inc.whew.android.fakegapps.FakeSignatures

class Hook4 : AbstractHook() {
    override val TAG: String = "FakeSignaturesHook"
    override fun init() {
        FakeSignatures.init()
    }
}