package org.microg.gms.potokens

import androidx.lifecycle.Lifecycle
import com.google.android.gms.common.api.internal.IStatusCallback
import com.google.android.gms.potokens.internal.IPoTokensService
import com.google.android.gms.potokens.internal.ITokenCallbacks

class PoTokensServiceImpl(
    private val packageName: String,
    private val helper: PoTokenHelper,
    val lifecycle: Lifecycle
) : IPoTokensService.Stub() {
    override fun responseStatus(call: IStatusCallback?, code: Int) {
    }

    override fun responseStatusToken(call: ITokenCallbacks?, i: Int, bArr: ByteArray?) {
    }

}