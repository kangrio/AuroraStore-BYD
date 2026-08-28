package com.kangrio.extension.gms

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.Feature
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.api.internal.IStatusCallback
import com.google.android.gms.common.internal.ConnectionInfo
import com.google.android.gms.common.internal.GetServiceRequest
import com.google.android.gms.common.internal.IGmsCallbacks
import com.google.android.gms.common.internal.IGmsServiceBroker
import com.google.android.gms.potokens.internal.IPoTokensService
import com.google.android.gms.potokens.internal.ITokenCallbacks
import org.microg.gms.BaseService
import org.microg.gms.common.GmsService
import org.microg.gms.potokens.PoTokenHelper
import org.microg.gms.potokens.PoTokensServiceImpl
import org.microg.gms.profile.ProfileManager

const val TAG = "PoTokensServiceProxy"
const val ACTION_NAME = "app.morphe.pot.helper.potokens.service.START"
const val PACKAGE_NAME = "app.morphe.pot.helper"
const val SERVICE_CLASS_NAME = "app.morphe.pot.helper.potokens.PoTokenService"
private val FEATURES = arrayOf(Feature("PO_TOKENS", 1))

class PoTokensServiceProxy : BaseService(TAG, GmsService.PO_TOKENS) {
    @Volatile
    private var iPoTokensService: IPoTokensService.Stub? = null
    override fun handleServiceRequest(callback: IGmsCallbacks, request: GetServiceRequest, service: GmsService) {
        Log.d(TAG, "PoTokensApiService handleServiceRequest")
        val intent = Intent(ACTION_NAME)
        val service = packageManager.resolveService(intent, 0)
        iPoTokensService = iPoTokensService ?: if (service == null) {
            PoTokensServiceImpl(request.packageName, PoTokenHelper(this), lifecycle)
        } else {
            PoTokensServiceProxyImpl(this, lifecycle)
        }

        ProfileManager.ensureInitialized(this)
        callback.onPostInitCompleteWithConnectionInfo(
            0,
            iPoTokensService,
            ConnectionInfo().apply { features = FEATURES }
        )
    }

    override fun onDestroy() {
        Log.d(TAG, "PoTokensApiService onDestroy")
        if (iPoTokensService is PoTokensServiceProxyImpl) {
            (iPoTokensService as? PoTokensServiceProxyImpl)?.destroy()
        }
        iPoTokensService = null
        super.onDestroy()
    }
}

class PoTokensServiceProxyImpl(
    val context: Context,
    override val lifecycle: Lifecycle
) : IPoTokensService.Stub(), LifecycleOwner {
    @Volatile
    private var iPoTokensService: IPoTokensService? = null
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iGmsServiceBroker = IGmsServiceBroker.Stub.asInterface(service)
            iGmsServiceBroker.getService(
                object : IGmsCallbacks.Stub() {
                    override fun onPostInitComplete(statusCode: Int, binder: IBinder?, params: Bundle?) {
                        Log.d(TAG, "onPostInitComplete")
                    }

                    override fun onAccountValidationComplete(statusCode: Int, params: Bundle?) {
                        Log.d(TAG, "onAccountValidationComplete")
                    }

                    override fun onPostInitCompleteWithConnectionInfo(statusCode: Int, binder: IBinder?, info: ConnectionInfo?) {
                        Log.d(TAG, "onPostInitCompleteWithConnectionInfo")
                        iPoTokensService = IPoTokensService.Stub.asInterface(binder)
                    }
                },
                GetServiceRequest(GmsService.PO_TOKENS.SERVICE_ID)
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            iPoTokensService = null
        }
    }

    init {
        if (iPoTokensService == null) {
            val intent = Intent(ACTION_NAME).apply {
                component = ComponentName(PACKAGE_NAME, SERVICE_CLASS_NAME)
            }

            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun destroy() {
        context.unbindService(serviceConnection)
    }

    override fun responseStatus(call: IStatusCallback, code: Int) {
        call.onResult(Status.SUCCESS)
    }

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        iPoTokensService?.let {
            return iPoTokensService!!.asBinder().transact(code, data, reply, flags)
        }

        return super.onTransact(code, data, reply, flags)
    }

    override fun responseStatusToken(call: ITokenCallbacks, i: Int, bArr: ByteArray) {
        lifecycleScope.launchWhenStarted {
            call.responseToken(Status.SUCCESS, null)
        }
    }
}