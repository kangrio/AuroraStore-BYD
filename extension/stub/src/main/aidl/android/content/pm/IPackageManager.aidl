package android.content.pm;

import android.content.pm.ApplicationInfo;

interface IPackageManager {
  String getInstallerPackageName(in String packageName);
  String[] getPackagesForUid(int uid);
  ApplicationInfo getApplicationInfo(String packageName, int flags, int userId);
  boolean hasSystemFeature4App(String packageName, int flags, String str);
  PackageInfo getPackageInfo(String packageName, int flags, int userId);
  PackageInfo getPackageInfoVersioned(in VersionedPackage versionedPackage, int flags, int userId);
  int checkPermission(String permName, String pkgName, int userId);
  InstallSourceInfo getInstallSourceInfo(in String packageName, int userId);
}