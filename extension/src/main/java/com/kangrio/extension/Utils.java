package com.kangrio.extension;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static String TAG = "Utils";

    private static List<String> KNOWN_GOOGLE_PACKAGES = Arrays.asList(
            "com.google.android.gms",
            "com.android.vending",
            "com.google.android.gsf"
    );

    private static String fakeGoogleSignatureData =
            "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMw\n" +
                    "EQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29n\n" +
                    "bGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEz\n" +
                    "MzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYw\n" +
                    "FAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5k\n" +
                    "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtW\n" +
                    "LgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKke\n" +
                    "dxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjA\n" +
                    "sb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/Cx\n" +
                    "URaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJE\n" +
                    "qO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/T\n" +
                    "gt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYD\n" +
                    "VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIG\n" +
                    "A1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC\n" +
                    "4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/y\n" +
                    "zKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCE\n" +
                    "yj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1S\n" +
                    "KMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifK\n" +
                    "Z0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3Wf\n" +
                    "MBEmh/9iFBDAaTCK\n";
    private static final ArrayMap<String, Pair<Signature, SigningInfo>> mSignatureCache = new ArrayMap<>();

    public static PackageInfo spoofSignature(PackageInfo packageInfo, String signatureData) {
        Pair<Signature, SigningInfo> cache = mSignatureCache.get(packageInfo.packageName);
        if (cache != null) {
            packageInfo.signatures = new Signature[]{ cache.first };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo = cache.second;
            }
            Log.d(TAG, "[CACHE] Spoofed signature: " + packageInfo.packageName + " " + sigToShar1(packageInfo.signatures[0]));
            return packageInfo;
        }

        if (KNOWN_GOOGLE_PACKAGES.contains(packageInfo.packageName)) {
            signatureData = fakeGoogleSignatureData;
        }

        byte[] signatureByte = Base64.decode(signatureData, Base64.DEFAULT);
        Signature signature = new Signature(signatureByte);
        packageInfo.signatures = new Signature[]{ signature };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final CertificateFactory certFactory;
            final Certificate cert;
            try {
                certFactory = CertificateFactory.getInstance("X.509");
                cert = certFactory.generateCertificate(new ByteArrayInputStream(signatureByte));
                packageInfo.signingInfo = createSigningInfo(signature, cert.getPublicKey());
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mSignatureCache.put(packageInfo.packageName, Pair.create(signature, packageInfo.signingInfo));
        } else {
            mSignatureCache.put(packageInfo.packageName, Pair.create(signature, null));
        }

        Log.d(TAG, "Spoofed signature: " + packageInfo.packageName + " " + sigToShar1(packageInfo.signatures[0]));
        return packageInfo;
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
                    new AbstractMap.SimpleEntry<Class<?>[], Object[]>(
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
                    new AbstractMap.SimpleEntry<Class<?>[], Object[]>(
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
            Log.w(TAG, "failed to create signingInfo");
            Log.w(TAG, e);
        }

        return null;
    }

    public static Field getField(Class<?> cls, String fieldName) {
        Class<?> currentClass = cls;
        while (currentClass != null) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    public static Object getFieldValue(Class<?> cls, String fieldName, Object instance) {
        try {
            Field field = getField(cls, fieldName);
            if (field == null) {
                return null;
            }

            return field.get(instance);
        } catch (Throwable e) {
            return null;
        }
    }

    @SafeVarargs
    public static <T> T invokeFirstConstructor(
            Class<T> cls,
            AbstractMap.SimpleEntry<Class<?>[], Object[]>... candidates
    ) throws ReflectiveOperationException {
        NoSuchMethodException exc = new NoSuchMethodException();
        for (AbstractMap.SimpleEntry<Class<?>[], Object[]> candidate : candidates) {
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

    public static Class<?> findFirstLoadableClass(String... candidates) throws ClassNotFoundException {
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
}
