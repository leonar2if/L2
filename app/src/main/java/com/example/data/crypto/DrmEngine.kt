package com.example.data.crypto

import java.security.MessageDigest

object DrmEngine {
    const val MASTER_KEY = "POS_MASTER_KEY_2026_xK9#mP2\$vL8_SecureToken"
    const val DEV_PASSWORD = "Leonar2.if"

    fun generateChallengeText(androidId: String, plan: String): String {
        val planTag = if (plan.contains("EMPRESA", ignoreCase = true)) "EMPRESA" else "PARTICULAR"
        return "DEV-$androidId-PLAN-$planTag"
    }

    fun calculateActivationPin(challengeQrText: String): String {
        val input = challengeQrText + MASTER_KEY
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))

        val digits = StringBuilder()
        for (b in digest) {
            val uVal = b.toInt() and 0xFF
            digits.append(uVal % 10)
        }
        return digits.toString().take(6).padStart(6, '0')
    }

    fun validatePin(challengeQrText: String, userPin: String): Boolean {
        val expected = calculateActivationPin(challengeQrText)
        return expected == userPin.trim()
    }
}
