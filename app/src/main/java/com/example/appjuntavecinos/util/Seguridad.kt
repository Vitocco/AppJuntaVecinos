package com.example.appjuntavecinos.util

import java.security.MessageDigest

object Seguridad {
    fun sha256(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
