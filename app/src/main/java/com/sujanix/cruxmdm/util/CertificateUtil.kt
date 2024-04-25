package com.sujanix.cruxmdm.util

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Collections


object CertificateUtil {
    private const val TAG = "CertificateUtil"

    /**
     * By enumerating the entries in a pkcs12 cert, find out the first entry that contain both private
     * key and certificate.
     *
     * @param contentResolver
     * @param uri uri of pkcs12 cert
     * @param password cert password
     * @return [PKCS12ParseInfo] which contains alias, x509 cert and private key, null if no
     * such an entry.
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     */
    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        CertificateException::class,
        UnrecoverableKeyException::class
    )
    fun parsePKCS12Certificate(
        contentResolver: ContentResolver,
        uri: Uri?,
        password: String
    ): PKCS12ParseInfo? {
        val inputStream = contentResolver.openInputStream(uri!!)
        val keystore = KeyStore.getInstance("PKCS12")
        keystore.load(inputStream, password.toCharArray())
        val aliases = keystore.aliases()
        // Find an entry contains both private key and user cert.
        for (alias in Collections.list(aliases)) {
            val privateKey = keystore.getKey(alias, "".toCharArray()) as PrivateKey
                ?: continue
            val clientCertificate = keystore.getCertificate(alias) as X509Certificate
                ?: continue
            Log.d(TAG, "parsePKCS12Certificate: $alias is selected")
            return PKCS12ParseInfo(alias, clientCertificate, privateKey)
        }
        return null
    }

    class PKCS12ParseInfo(
        var alias: String,
        var certificate: X509Certificate,
        var privateKey: PrivateKey
    )
}
