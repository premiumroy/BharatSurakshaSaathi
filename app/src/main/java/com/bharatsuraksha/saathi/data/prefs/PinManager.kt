package com.bharatsuraksha.saathi.data.prefs

import java.security.MessageDigest

/**
 * Parent PIN handling for Kids Safe Mode. We store only a salted SHA-256 hash,
 * never the PIN itself. This is obfuscation against casual snooping, not
 * military-grade security — the PIN protects a setting, not your money.
 */
object PinManager {

    private const val SALT = "bharat_suraksha_saathi_pin_v1"

    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((SALT + pin).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun isValidPin(pin: String): Boolean = pin.length in 4..6 && pin.all { it.isDigit() }
}
