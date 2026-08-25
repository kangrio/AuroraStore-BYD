package inc.whew.android.fakegapps;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.AbstractMap.SimpleEntry;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class FakeSignatures {
    private static final String TAG = "FakeSignatures";
    private static final String _GMSx509cert = "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKkedxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjAsb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/CxURaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJEqO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/Tgt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/yzKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCEyj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1SKMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifKZ0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3WfMBEmh/9iFBDAaTCK";

    private static final String[] gmsBundle = new String[]{ "com.google.android.gms", "com.android.vending" };

    public static void init() {
        XposedBridge.disableHiddenApiRestrictions();
        packageInfoHook();
        getInstallerPackageNameHook();
        pairipHook();
    }

    private static void hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        try {
            XposedBridge.hookAllMethods(hookClass, methodName, callback);
            log("Hooked " + hookClass.getName() + "." + methodName);
        } catch (Throwable e) {
            log(e);
        }
    }

    private static void hookAllMethods(String hookClassName, String methodName, XC_MethodHook callback) {
        try {
            Class<?> hookClass = Class.forName(hookClassName);
            hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable e) {
            log(e);
        }
    }

    private static void pairipHook() {
        XC_MethodHook processResponseHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.args[0] = 0;
            }
        };

        hookAllMethods("com.pairip.licensecheck.ResponseValidator", "validateResponse", XC_MethodReplacement.DO_NOTHING);
        hookAllMethods("com.pairip.licensecheck.LicenseClient", "processResponse", processResponseHook);

        hookAllMethods("com.pairip.licensecheck3.ResponseValidator", "validateResponse", XC_MethodReplacement.DO_NOTHING);
        hookAllMethods("com.pairip.licensecheck3.LicenseClientV3", "processResponse", processResponseHook);
    }

    private static void packageInfoHook() {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.args[1] = ((int) param.args[1]) | PackageManager.GET_META_DATA;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws CertificateException {
                int flags = (int) param.args[1];
                boolean hasSignatureFlag = (flags & PackageManager.GET_SIGNATURES) != 0
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && (flags & PackageManager.GET_SIGNING_CERTIFICATES) != 0);
                if (!hasSignatureFlag) {
                    return;
                }

                PackageInfo pi = (PackageInfo) param.getResult();
                if (pi != null && pi.applicationInfo != null && pi.applicationInfo.metaData != null) {
                    Bundle metaData = pi.applicationInfo.metaData;
                    final String _x509cert = getX509cert(pi.packageName, metaData);
                    if (_x509cert == null) {
                        return;
                    }


                    final byte[] certBytes = Base64.decode(_x509cert, Base64.DEFAULT);
                    final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    final Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

                    pi.signatures = new Signature[]{ new Signature(certBytes) };
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        SigningInfo signingInfo = createSigningInfo(new Signature(certBytes), cert.getPublicKey());
                        if (signingInfo != null) {
                            pi.signingInfo = signingInfo;
                        }
                    }

                    Log.d(TAG, "Spoofed signature: " + pi.packageName + " " + sigToShar1(pi.signatures[0]));
                    param.setResult(pi);
                }
            }
        };

        hookAllMethods("android.app.ApplicationPackageManager", "getPackageInfo", hook);
    }

    private static void getInstallerPackageNameHook() {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult("com.android.vending");
            }
        };

        hookAllMethods("android.app.ApplicationPackageManager", "getInstallerPackageName", hook);
        hookAllMethods("android.content.pm.InstallSourceInfo", "getInstallingPackageName", hook);
    }

    private static String getX509cert(String packageName, Bundle metaData) {
        boolean isGms = false;
        for (String pkg : gmsBundle) {
            if (pkg.equals(packageName)) {
                isGms = true;
                break;
            }
        }

        final String _x509cert;
        if (isGms) {
            _x509cert = _GMSx509cert;
        } else {
            _x509cert = metaData.getString("org.microg.gms.spoofed_certificates");
        }
        return _x509cert;
    }

    public static String sigToShar1(Signature sig) {
        // to shar1 string
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(sig.toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static Class<?> findFirstLoadableClass(String... candidates) throws ClassNotFoundException {
        ClassNotFoundException exc = new ClassNotFoundException();
        for (String candidate : candidates) {
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException e) {
                exc = e;
            }
        }
        throw exc;
    }

    private static <T> T invokeFirstConstructor(
            Class<T> cls,
            SimpleEntry<Class<?>[], Object[]>... candidates
    ) throws ReflectiveOperationException {
        NoSuchMethodException exc = new NoSuchMethodException();
        for (SimpleEntry<Class<?>[], Object[]> candidate : candidates) {
            Constructor<T> constructor;
            try {
                constructor = cls.getDeclaredConstructor(candidate.getKey());
            } catch (NoSuchMethodException e) {
                exc = e;
                continue;
            }

            constructor.setAccessible(true);
            return constructor.newInstance(candidate.getValue());
        }
        throw exc;
    }

    @TargetApi(android.os.Build.VERSION_CODES.P)
    private static SigningInfo createSigningInfo(Signature sig, PublicKey publicKey) {
        final int SIGNING_BLOCK_V3 = 3;
        final Signature[] sigs = new Signature[]{sig};
        final ArraySet<PublicKey> pks = new ArraySet<>();
        pks.add(publicKey);

        // Unfortunately, SigningDetails is not exported in SDK, so we have to rely on reflection.
        // Also, public SigningInfo constructor is only available from API 35, so we can't use it.
        try {
            Class<?> signingDetailsClass = findFirstLoadableClass(
                    "android.content.pm.SigningDetails",
                    // Android 9 to 12 have SigningDetails embedded in the PackageParser class
                    "android.content.pm.PackageParser$SigningDetails"
            );
            Object signingDetails = invokeFirstConstructor(
                    signingDetailsClass,
                    // https://cs.android.com/android/platform/superproject/+/android-15.0.0_r17:frameworks/base/core/java/android/content/pm/SigningDetails.java;l=146
                    new SimpleEntry<Class<?>[], Object[]>(
                            new Class<?>[]{
                                    Signature[].class, // signatures
                                    int.class, // signatureSchemeVersion
                                    ArraySet.class, // keys
                                    Signature[].class // pastSigningCertificates
                            },
                            new Object[]{sigs, SIGNING_BLOCK_V3, pks, null}
                    ),
                    // Android 9 had an extra "pastSigningCertificatesFlags" argument
                    // https://cs.android.com/android/platform/superproject/+/android-9.0.0_r60:frameworks/base/core/java/android/content/pm/PackageParser.java;l=5739
                    new SimpleEntry<Class<?>[], Object[]>(
                            new Class<?>[]{
                                    Signature[].class, // signatures
                                    int.class, // signatureSchemeVersion
                                    ArraySet.class, // keys
                                    Signature[].class, // pastSigningCertificates
                                    int[].class // pastSigningCertificatesFlags
                            },
                            new Object[]{sigs, SIGNING_BLOCK_V3, pks, null, null}
                    )
            );

            Constructor<SigningInfo> signingInfoConstructor = SigningInfo.class.getDeclaredConstructor(signingDetailsClass);
            signingInfoConstructor.setAccessible(true);
            return signingInfoConstructor.newInstance(signingDetails);
        } catch (Exception e) {
            log(String.format("%s failed to create signingInfo", TAG));
            log(e);
        }

        return null;
    }

    static void log(Object msg) {
        Log.v(TAG, msg.toString());
    }
}