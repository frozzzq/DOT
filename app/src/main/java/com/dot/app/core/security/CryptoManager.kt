package com.dot.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor criptográfico local bajo enfoque Zero-Knowledge (RNF-01).
 *
 * Utiliza Android Keystore respaldado por hardware para resguardar una llave simétrica
 * AES de 256 bits en modo GCM (Galois/Counter Mode) sin relleno, garantizando
 * confidencialidad e integridad autenticada (AEAD) antes de cualquier persistencia
 * externa o sincronización hacia AWS.
 */
@Singleton
class CryptoManager @Inject constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Cifra un arreglo de bytes en claro y devuelve un payload compuesto por:
     * [1 byte longitud IV] + [IV (12 bytes)] + [Ciphertext + GCM Auth Tag (128 bits)].
     */
    fun encrypt(plainBytes: ByteArray, associatedData: ByteArray? = null): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        associatedData?.let { cipher.updateAAD(it) }

        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainBytes)

        return ByteBuffer.allocate(1 + iv.size + cipherBytes.size)
            .put(iv.size.toByte())
            .put(iv)
            .put(cipherBytes)
            .array()
    }

    /**
     * Descifra un payload previamente generado por [encrypt] y verifica su etiqueta
     * de autenticación GCM.
     */
    fun decrypt(encryptedPayload: ByteArray, associatedData: ByteArray? = null): ByteArray {
        val buffer = ByteBuffer.wrap(encryptedPayload)
        val ivLength = buffer.get().toInt()
        require(ivLength in 12..16) { "Longitud de vector de inicialización (IV) inválida." }

        val iv = ByteArray(ivLength)
        buffer.get(iv)

        val cipherBytes = ByteArray(buffer.remaining())
        buffer.get(cipherBytes)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
        associatedData?.let { cipher.updateAAD(it) }

        return cipher.doFinal(cipherBytes)
    }

    /**
     * Cifra una cadena UTF-8 y devuelve el resultado codificado en Base64 seguro para transporte.
     */
    fun encryptToString(plainText: String, associatedData: ByteArray? = null): String {
        val encrypted = encrypt(plainText.toByteArray(StandardCharsets.UTF_8), associatedData)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * Descifra una cadena en Base64 generada por [encryptToString] a texto en claro UTF-8.
     */
    fun decryptFromString(base64CipherText: String, associatedData: ByteArray? = null): String {
        val decoded = Base64.decode(base64CipherText, Base64.NO_WRAP)
        val plainBytes = decrypt(decoded, associatedData)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "dot_zero_knowledge_aes256_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BITS = 256
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
