package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * The family code word, stored encrypted on this phone with a key held in the
 * Android Keystore. It is never written in the clear and never leaves the
 * device: there is no server and no sync.
 */
object CodeWordStore {

    private const val PREFS = "saathi_secure"
    private const val KEY_NAME = "code_word"
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "saathi_code_word_key"
    private const val TRANSFORM = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12
    private const val TAG_BITS = 128

    private val _word = MutableStateFlow<String?>(null)
    val word: StateFlow<String?> = _word

    fun load(context: Context) {
        val blob = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NAME, null)
        _word.value = if (blob == null) null else decrypt(blob)
    }

    fun set(context: Context, value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            clear(context)
            return
        }
        _word.value = trimmed
        val blob = encrypt(trimmed) ?: return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, blob)
            .apply()
    }

    fun clear(context: Context) {
        _word.value = null
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_NAME).apply()
    }

    // ---------------------------------------------------------------- crypto

    private fun secretKey(): SecretKey? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        return try {
            val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
            (store.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
                    init(
                        KeyGenParameterSpec.Builder(
                            ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
                    )
                }.generateKey()
        } catch (e: Exception) {
            null
        }
    }

    private fun encrypt(plain: String): String? {
        val key = secretKey() ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(iv + body, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun decrypt(blob: String): String? {
        val key = secretKey() ?: return null
        return try {
            val bytes = Base64.decode(blob, Base64.NO_WRAP)
            if (bytes.size <= IV_LENGTH) return null
            val iv = bytes.copyOfRange(0, IV_LENGTH)
            val body = bytes.copyOfRange(IV_LENGTH, bytes.size)
            val cipher = Cipher.getInstance(TRANSFORM)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(body), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
