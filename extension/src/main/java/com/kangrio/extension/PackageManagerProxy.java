package com.kangrio.extension;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

@SuppressLint("SoonBlockedPrivateApi, BlockedPrivateApi")
public class PackageManagerProxy extends Binder {
    static String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    static int UNDEFENDED = -1;

    IPackageManager iPackageManager;
    public IBinder iBinder;
    int getPackageInstallerCode = UNDEFENDED;
    int getInstallSourceInfoCode = UNDEFENDED;

    public PackageManagerProxy() {
        this.iBinder = ServiceManager.getService("package");
        this.iPackageManager = IPackageManager.Stub.asInterface(iBinder);
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        int position = data.dataPosition();
        data.enforceInterface(IPackageManager.Stub.DESCRIPTOR);
        String pkg = data.readString();
        data.setDataPosition(position);

        if (pkg == null || isSystemApp(pkg)) {
            return iBinder.transact(code, data, reply, flags);
        }

        ensureTransactionCode();
        if (code == getPackageInstallerCode) {
            return getInstallerPackageName(code, data, reply, flags);
        }

        if (code == getInstallSourceInfoCode) {
            return getInstallSourceInfo(code, data, reply, flags);
        }

        return iBinder.transact(code, data, reply, flags);
    }

    void ensureTransactionCode() {
        if (getPackageInstallerCode == UNDEFENDED) {
            try {
                Field field = IPackageManager.Stub.class.getDeclaredField("TRANSACTION_getInstallerPackageName");
                field.setAccessible(true);
                getPackageInstallerCode = field.getInt(null);
            } catch (Throwable ignore) {
            }
        }
        if (getInstallSourceInfoCode == UNDEFENDED) {
            try {
                Field field = IPackageManager.Stub.class.getDeclaredField("TRANSACTION_getInstallSourceInfo");
                field.setAccessible(true);
                getInstallSourceInfoCode = field.getInt(null);
            } catch (Throwable ignore) {
            }
        }
    }

    private boolean getInstallSourceInfo(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return iBinder.transact(getInstallSourceInfoCode, data, reply, flags);
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
