// `[data-rm-k]` payload decoder used by the newer DiziPal templates.
// CryptoJS: PBKDF2-HMAC-SHA512 (999 rounds, 256 bit) + AES-CBC.

package com.panates

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.base64DecodeArray
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object DpRmk {
    // Password shipped in the site bundle (app-dizipals.js); rotate here if it changes.
    private const val PAROLA =
        "3hPn4uCjTVtfYWcjIcoJQ4cL1WWk1qxXI39egLYOmNv6IblA7eKJz68uU3eLzux1biZLCms0quEjTYniGv5z1JcKbNIsDQFSeIZOBZJz4is6pD7UyWDggWWzTLBQbHcQFpBQdClnuQaMNUHtLHTpzCvZy33p6I7wFBvL4fnXBYH84aUIyWGTRvM2G5cfoNf4705tO2kv"

    private val mapper by lazy { ObjectMapper() }

    data class DpRmkVeri(
        @JsonProperty("ciphertext") val ciphertext: String? = null,
        @JsonProperty("iv") val iv: String? = null,
        @JsonProperty("salt") val salt: String? = null
    )

    /** Finds a `data-rm-k` attribute in the page and returns the decrypted player URL. */
    fun decryptFromPage(html: String): String? {
        val payload = Regex("""data-rm-k=["']([^"']+)["']""").find(html)?.groupValues?.get(1)
            ?: return null
        return decrypt(payload)
    }

    fun decrypt(json: String): String? = runCatching {
        val veri = mapper.readValue(json, DpRmkVeri::class.java)
        val salt = hexToBytes(veri.salt ?: return null)
        val iv = hexToBytes(veri.iv ?: return null)
        val ct = base64DecodeArray(veri.ciphertext ?: return null)
        val key = pbkdf2Sha512(PAROLA.toByteArray(Charsets.UTF_8), salt, 999, 32)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        String(cipher.doFinal(ct), Charsets.UTF_8)
    }.getOrNull()

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        return ByteArray(clean.length / 2) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    /** PBKDF2WithHmacSHA512 is missing on older Android releases, so it is computed by hand. */
    private fun pbkdf2Sha512(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(password, "HmacSHA512"))

        val result = ByteArray(keyLength)
        var block = 1
        var position = 0

        while (position < keyLength) {
            mac.update(salt)
            mac.update(
                byteArrayOf(
                    (block ushr 24).toByte(),
                    (block ushr 16).toByte(),
                    (block ushr 8).toByte(),
                    block.toByte()
                )
            )
            var u = mac.doFinal()
            val t = u.copyOf()

            for (i in 1 until iterations) {
                u = mac.doFinal(u)
                for (j in t.indices) t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
            }

            val copy = minOf(t.size, keyLength - position)
            System.arraycopy(t, 0, result, position, copy)
            position += copy
            block++
        }

        return result
    }
}