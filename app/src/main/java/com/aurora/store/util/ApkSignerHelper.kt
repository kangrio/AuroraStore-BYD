package com.aurora.store.util

import android.content.Context
import com.android.apksig.ApkSigner
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date

object ApkSignerHelper {
    private val PASSWORD = "12345678".toCharArray()
    private const val ALIAS = "AuroraStore"

    fun signApk(
        context: Context,
        inputApk: File,
        outputApk: File,
    ) {
        val ks = KeyStore.getInstance("PKCS12")

        val keystoreFile = File(context.filesDir, "keystore/keystore.p12")

        if (keystoreFile.exists() && keystoreFile.readLines().isNotEmpty()) {
            FileInputStream(keystoreFile).use {
                ks.load(it, PASSWORD)
            }
        } else {
            keystoreFile.parentFile?.mkdirs()

            val (key, cert) = generateKeyPairAndCert()
            ks.load(null, null)
            ks.setKeyEntry(ALIAS, key, PASSWORD, arrayOf(cert))

            FileOutputStream(keystoreFile).use { fos ->
                ks.store(fos, PASSWORD)
            }
        }

        val privateKey = ks.getKey(ALIAS, PASSWORD) as PrivateKey
        val cert = ks.getCertificate(ALIAS) as X509Certificate

        val signerConfig = ApkSigner.SignerConfig.Builder(
            "CERT",
            privateKey,
            listOf(cert)
        ).build()

        ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setMinSdkVersion(-1)
            .build()
            .sign()
    }

    fun generateKeyPairAndCert(): Pair<PrivateKey, X509Certificate> {
        // 1. Generate RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048, SecureRandom())
        val keyPair = keyPairGenerator.generateKeyPair()

        // 2. Define certificate metadata
        val subject = X500Name("CN=$ALIAS, O=Android, C=US")
        val serial = BigInteger.valueOf(System.currentTimeMillis())
        val notBefore = Date()
        val notAfter = Date(System.currentTimeMillis() + 30 * 365L * 24 * 60 * 60 * 1000) // 30 year

        // 3. Build the certificate
        val certBuilder: X509v3CertificateBuilder = JcaX509v3CertificateBuilder(
            subject,       // issuer (self-signed, so same as subject)
            serial,
            notBefore,
            notAfter,
            subject,       // subject
            keyPair.public
        )

        // 4. Sign with the private key (SHA256withRSA)
        val signer = JcaContentSignerBuilder("SHA256withRSA")
            .build(keyPair.private)

        val certHolder = certBuilder.build(signer)

        // 5. Convert to JCA X509Certificate
        val certificate = JcaX509CertificateConverter()
            .getCertificate(certHolder)

        return Pair(keyPair.private, certificate)
    }
}