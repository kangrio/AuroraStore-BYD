package org.microg.gms;

import android.os.RemoteException;

import androidx.lifecycle.LifecycleService;

import com.google.android.gms.common.internal.GetServiceRequest;
import com.google.android.gms.common.internal.IGmsCallbacks;

import org.microg.gms.common.GmsService;

public abstract class BaseService extends LifecycleService {
    public BaseService(String tag, GmsService supportedService, GmsService... supportedServices) {
    }
    public abstract void handleServiceRequest(IGmsCallbacks callback, GetServiceRequest request, GmsService service) throws RemoteException;
}
