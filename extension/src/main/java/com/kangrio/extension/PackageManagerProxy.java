package com.kangrio.extension;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

@SuppressLint({"SoonBlockedPrivateApi, BlockedPrivateApi", "DiscouragedPrivateApi"})
public class PackageManagerProxy extends Binder {
    private static final String TAG = "PackageManagerProxy";
    private static final String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    private static final String PACKAGE_MANAGER_SERVICE_NAME = "package";
    private static final String PACKAGE_MANAGER_FIELD_NAME = "sPackageManager";
    private static final String SERVICE_CACHE_FIELD_NAME = "sCache";
    private static final String SPOOF_CERT_META_DATA_KEY = "org.microg.gms.spoofed_certificates";
    private static final int UNDEFINED = -1;

    private final IPackageManager iPackageManager;
    private final IBinder iBinder;
    private int getPackageInstallerCode = UNDEFINED;
    private int getInstallSourceInfoCode = UNDEFINED;
    private int getPackageInfoCode = UNDEFINED;

    public static void init() {
        try {
            PackageManagerProxy proxy = new PackageManagerProxy();
            Field sCacheField = ServiceManager.class.getDeclaredField(SERVICE_CACHE_FIELD_NAME);
            sCacheField.setAccessible(true);
            ArrayMap<String, IBinder> sCache = (ArrayMap<String, IBinder>) sCacheField.get(null);
            sCache.clear();
            sCache.put(PACKAGE_MANAGER_SERVICE_NAME, proxy);

            IPackageManager iPackageManagerProxy = IPackageManager.Stub.asInterface(proxy);
            Field sPackageManagerField = ActivityThread.class.getDeclaredField(PACKAGE_MANAGER_FIELD_NAME);
            sPackageManagerField.setAccessible(true);
            sPackageManagerField.set(null, iPackageManagerProxy);
            Log.d(TAG, "init success");
        } catch (Throwable e) {
            Log.e(TAG, "init fail: ", e);
        }
    }

    PackageManagerProxy() {
        this.iBinder = ServiceManager.getService(PACKAGE_MANAGER_SERVICE_NAME);
        this.iPackageManager = IPackageManager.Stub.asInterface(iBinder);
    }

    private boolean transactOriginal(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return iBinder.transact(code, data, reply, flags);
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        int position = data.dataPosition();
        data.enforceInterface(IPackageManager.Stub.DESCRIPTOR);
        String pkg = data.readString();
        data.setDataPosition(position);

        if (pkg == null || isSystemApp(pkg)) {
            return transactOriginal(code, data, reply, flags);
        }

        ensureTransactionCode();
        if (code == getPackageInstallerCode) {
            return getInstallerPackageName(code, data, reply, flags);
        }

        if (code == getInstallSourceInfoCode) {
            return getInstallSourceInfo(code, data, reply, flags);
        }

        if (code == getPackageInfoCode) {
            return getPackageInfo(code, data, reply, flags);
        }

        return transactOriginal(code, data, reply, flags);
    }

    void ensureTransactionCode() {
        if (getPackageInstallerCode == UNDEFINED) {
            try {
                Field field = IPackageManager.Stub.class.getDeclaredField("TRANSACTION_getInstallerPackageName");
                field.setAccessible(true);
                getPackageInstallerCode = field.getInt(null);
            } catch (Throwable ignore) {
            }
        }
        if (getInstallSourceInfoCode == UNDEFINED) {
            try {
                Field field = IPackageManager.Stub.class.getDeclaredField("TRANSACTION_getInstallSourceInfo");
                field.setAccessible(true);
                getInstallSourceInfoCode = field.getInt(null);
            } catch (Throwable ignore) {
            }
        }
        if (getPackageInfoCode == UNDEFINED) {
            try {
                Field field = IPackageManager.Stub.class.getDeclaredField("TRANSACTION_getPackageInfo");
                field.setAccessible(true);
                getPackageInfoCode = field.getInt(null);
            } catch (Throwable ignore) {
            }
        }
    }

    private boolean getPackageInfo(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        int position = data.dataPosition();
        data.enforceInterface(IPackageManager.Stub.DESCRIPTOR);
        String pkg = data.readString();
        int flags2 = data.readInt();
        data.setDataPosition(position);

        if ((flags2 & PackageManager.GET_SIGNATURES) == 0 && (flags2 & PackageManager.GET_SIGNING_CERTIFICATES) == 0) {
            return transactOriginal(code, data, reply, flags);
        }

        PackageInfo packageInfo = iPackageManager.getPackageInfo(pkg, PackageManager.GET_META_DATA, 0);
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return transactOriginal(code, data, reply, flags);
        }

        Bundle metaData = packageInfo.applicationInfo.metaData;
        if (metaData == null) {
            return transactOriginal(code, data, reply, flags);
        }

        String signatureData = metaData.getString(SPOOF_CERT_META_DATA_KEY);
        if (signatureData == null) {
            return transactOriginal(code, data, reply, flags);
        }

        packageInfo = Utils.spoofSignature(packageInfo, signatureData);
        reply.writeNoException();
        reply.writeInt(1);
        packageInfo.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

        return true;
    }

    private boolean getInstallSourceInfo(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return transactOriginal(code, data, reply, flags);
        }

        // https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/content/pm/InstallSourceInfo.java#71
        Parcel newSource = Parcel.obtain();
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mInitiatingPackageName
        newSource.writeParcelable(null, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);  // mInitiatingPackageSigningInfo
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mOriginatingPackageName
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mInstallingPackageName
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mUpdateOwnerPackageName
        newSource.setDataPosition(0);

        InstallSourceInfo info = InstallSourceInfo.CREATOR.createFromParcel(newSource);
        newSource.recycle();
        reply.writeNoException();
        reply.writeInt(1);
        info.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        return true;
    }

    private boolean getInstallerPackageName(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) {
        reply.writeNoException();
        reply.writeString(PLAY_STORE_PACKAGE_NAME);
        return true;
    }

    private boolean isSystemApp(String pkg) throws RemoteException {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }

        PackageInfo packageInfo = iPackageManager.getPackageInfo(pkg, 0, 0);
        ApplicationInfo appInfo = packageInfo != null ? packageInfo.applicationInfo : null;

        return appInfo != null && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
