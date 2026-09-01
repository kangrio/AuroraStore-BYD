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
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressLint({"SoonBlockedPrivateApi, BlockedPrivateApi", "DiscouragedPrivateApi"})
public class PackageManagerProxy extends Binder {
    private static final String TAG = "PackageManagerProxy";
    private static final String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    private static final String PACKAGE_MANAGER_SERVICE_NAME = "package";
    private static final String PACKAGE_MANAGER_FIELD_NAME = "sPackageManager";
    private static final String SERVICE_CACHE_FIELD_NAME = "sCache";
    private static final String SPOOF_CERT_META_DATA_KEY = "org.microg.gms.spoofed_certificates";

    private final IPackageManager iPackageManager;
    private final IBinder iBinder;
    private final Map<Integer, String> transactionCodes;
    private int userId = 0;

    public static void init() {
        try {
            PackageManagerProxy proxy = new PackageManagerProxy();
            ArrayMap<String, IBinder> sCache = (ArrayMap<String, IBinder>) Utils.getFieldValue(ServiceManager.class, SERVICE_CACHE_FIELD_NAME, null);
            if (sCache != null) {
                sCache.put(PACKAGE_MANAGER_SERVICE_NAME, proxy);
            }

            IPackageManager iPackageManagerProxy = IPackageManager.Stub.asInterface(proxy);
            Field sPackageManagerField = Utils.getField(ActivityThread.class, PACKAGE_MANAGER_FIELD_NAME);
            if (sPackageManagerField != null) {
                sPackageManagerField.set(null, iPackageManagerProxy);
            }
            Log.d(TAG, "init success");
        } catch (Throwable e) {
            Log.e(TAG, "init fail: ", e);
        }
    }

    private PackageManagerProxy() {
        this.iBinder = ServiceManager.getService(PACKAGE_MANAGER_SERVICE_NAME);
        this.iPackageManager = IPackageManager.Stub.asInterface(iBinder);
        this.transactionCodes = initTransactionCodes();

        try{
            this.userId = (int) UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
        } catch (Throwable ignore) {}
    }

    private boolean transactOriginal(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return iBinder.transact(code, data, reply, flags);
    }

    @Override
    public String getInterfaceDescriptor() {
        return IPackageManager.Stub.DESCRIPTOR;
    }

    private Map<Integer, String> initTransactionCodes() {
        Map<Integer, String> result = new HashMap<>();

        for (Field field : IPackageManager.Stub.class.getDeclaredFields()) {
            if (!field.getName().startsWith("TRANSACTION_")) {
                continue;
            }

            if (field.getType() != int.class) {
                continue;
            }

            try {
                field.setAccessible(true);
                int code = field.getInt(null);

                result.put(code, field.getName().replace("TRANSACTION_", ""));
            } catch (Throwable ignored) {
            }
        }

        return result;
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        String transaction = transactionCodes.get(code);
        if (transaction == null) {
            return transactOriginal(code, data, reply, flags);
        }

        return switch (transaction) {
            case "getInstallerPackageName" -> getInstallerPackageName(code, data, reply, flags);
            case "getInstallSourceInfo" -> getInstallSourceInfo(code, data, reply, flags);
            case "getPackageInfo" -> getPackageInfo(code, data, reply, flags);

            default -> transactOriginal(code, data, reply, flags);
        };
    }

    private boolean getPackageInfo(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (isSystemApp(code, data, reply, flags)) {
            return transactOriginal(code, data, reply, flags);
        }

        int position = data.dataPosition();
        data.enforceInterface(IPackageManager.Stub.DESCRIPTOR);
        String pkg = data.readString();

        // Android 13+ uses long flags.
        // https://android.googlesource.com/platform/frameworks/base/+/android13-release/core/java/android/content/pm/IPackageManager.aidl#72
        final long flags2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            flags2 = data.readLong();
        } else {
            flags2 = Integer.toUnsignedLong(data.readInt());
        }
        data.setDataPosition(position);

        if ((flags2 & PackageManager.GET_SIGNATURES) == 0 && (flags2 & PackageManager.GET_SIGNING_CERTIFICATES) == 0) {
            return transactOriginal(code, data, reply, flags);
        }

        PackageInfo packageInfo = getPackageInfoInternal(pkg, PackageManager.GET_META_DATA);
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
        reply.setDataPosition(0);
        return true;
    }

    private PackageInfo getPackageInfoInternal(String pkg, int flags) {
        try {
            Method method = iPackageManager.getClass().getMethod(
                    "getPackageInfo",
                    String.class,
                    Build.VERSION.SDK_INT >= 33 ? long.class : int.class,
                    int.class
            );

            Object packageInfo = Build.VERSION.SDK_INT >= 33
                    ? method.invoke(iPackageManager, pkg, (long) flags, userId)
                    : method.invoke(iPackageManager, pkg, flags, userId);

            return (PackageInfo) packageInfo;
        } catch (Throwable e) {
            return null;
        }
    }

    private boolean getInstallSourceInfo(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return transactOriginal(code, data, reply, flags);
        }

        if (isSystemApp(code, data, reply, flags)) {
            return transactOriginal(code, data, reply, flags);
        }

        // https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/content/pm/InstallSourceInfo.java#71
        Parcel newSource = Parcel.obtain();
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mInitiatingPackageName
        newSource.writeParcelable(null, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);     // mInitiatingPackageSigningInfo
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mOriginatingPackageName
        newSource.writeString(PLAY_STORE_PACKAGE_NAME);                                // mInstallingPackageName
        try {
            // mUpdateOwnerPackageName
            Parcel.class.getDeclaredMethod("writeString8", String.class).invoke(newSource, PLAY_STORE_PACKAGE_NAME);
        } catch (Throwable ignore) {
        }
        newSource.setDataPosition(0);

        InstallSourceInfo info = InstallSourceInfo.CREATOR.createFromParcel(newSource);
        newSource.recycle();
        reply.writeNoException();
        reply.writeInt(1);
        info.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        reply.setDataPosition(0);
        return true;
    }

    private boolean getInstallerPackageName(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (isSystemApp(code, data, reply, flags)) {
            return transactOriginal(code, data, reply, flags);
        }

        reply.writeNoException();
        reply.writeString(PLAY_STORE_PACKAGE_NAME);
        reply.setDataPosition(0);
        return true;
    }

    private boolean isSystemApp(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        int position = data.dataPosition();
        data.enforceInterface(IPackageManager.Stub.DESCRIPTOR);
        String pkg = data.readString();
        data.setDataPosition(position);

        if (pkg == null || pkg.isEmpty()) {
            return false;
        }

        PackageInfo packageInfo = getPackageInfoInternal(pkg, 0);
        ApplicationInfo appInfo = packageInfo != null ? packageInfo.applicationInfo : null;

        return appInfo != null && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
