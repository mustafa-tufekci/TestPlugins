package com.panates

import android.util.Base64
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Byse (bysejikuar.com) player used by sezonlukdizi for its "Filemoon" servers.
 *
 * Flow (reverse engineered from the Byse frontend bundles):
 * 1. POST /api/videos/{code}/embed/captcha           -> pow_nonce, pow_difficulty, pow_token
 * 2. Solve proof-of-work: the custom 32-bit Gr hash of
 *    "<pow_nonce>:<counter>" must have >= pow_difficulty leading zero bits;
 *    the solution is the counter as decimal string.
 * 3. POST /api/videos/{code}/embed/captcha/verify    {"pow_token":..,"solution":..} -> {"token":..}
 * 4. POST /api/videos/{code}/embed/playback          header X-Captcha-Token -> encrypted playback JSON
 * 5. AES-256-GCM decrypt (key = key_parts[version-1] + key_parts[30-version] base64url decoded)
 *    -> {"sources":[{"url":"...m3u8",...}],"tracks":[...]}
 */
class FilemoonSez : ExtractorApi() {
    override val name            = "FilemoonSez"
    override val mainUrl         = "https://bysejikuar.com"
    override val requiresReferer = false

    private val powTimeoutMs = 30_000L

    private val POST_HEADERS = mapOf(
        "Accept" to "application/json",
        "Referer" to "https://sezonlukdizi.cc/",
        "Origin" to "https://sezonlukdizi.cc"
    )

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val code = url.substringAfterLast("/e/").substringBefore("?").trim()
            if (code.isBlank()) return

            // 1) captcha challenge
            val captchaNode = postJson("$mainUrl/api/videos/$code/embed/captcha", "{}")
                ?.let { ObjectMapper().readTree(it) } ?: return

            val powNonce = captchaNode.path("pow_nonce").asText(null) ?: return
            val powDifficulty = captchaNode.path("pow_difficulty").asInt(16)
            val powToken = captchaNode.path("pow_token").asText(null) ?: return

            // 2) solve PoW
            val solution = solvePow(powNonce, powDifficulty) ?: return

            // 3) verify
            val verifyBody = """{"pow_token":${jsonString(powToken)},"solution":"$solution"}"""
            val verifyNode = postJson("$mainUrl/api/videos/$code/embed/captcha/verify", verifyBody)
                ?.let { ObjectMapper().readTree(it) } ?: return

            if (verifyNode.path("status").asText("") != "ok") return
            val captchaToken = verifyNode.path("token").asText(null) ?: return

            // 4) playback (fingerprint values are accepted as-is by the server)
            val playbackBody = """{"fingerprint":{"token":"x","viewer_id":"x","device_id":"x","confidence":0.0}}"""
            val playbackNode = postJson(
                "$mainUrl/api/videos/$code/embed/playback",
                playbackBody,
                extraHeaders = mapOf("X-Captcha-Token" to captchaToken)
            )?.let { ObjectMapper().readTree(it) } ?: return

            val pb = playbackNode.path("playback")
            if (pb.isMissingNode) return

            // 5) AES-256-GCM decrypt
            val plain = decryptPlayback(pb) ?: return

            val sources = plain.path("sources")
            if (sources.isArray) {
                // Skill invariant: one master per server/audio variant. Byse normally returns a
                // single master; if it ever lists separate qualities we keep only the best one
                // per mime type instead of publishing 1080p/720p/480p as separate sources.
                val picked = sources.mapNotNull { source ->
                    val srcUrl = source.path("url").asText(null)?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    val label = source.path("label").asText("Byse")
                    val height = source.path("height").asInt(0).takeIf { it > 0 }
                        ?: Regex("\\d+").find(label)?.value?.toIntOrNull()
                        ?: 0
                    ByseSource(
                        mime = source.path("mime_type").asText("application/vnd.apple.mpegurl"),
                        url = srcUrl,
                        label = label,
                        height = height
                    )
                }
                    .groupBy { it.mime }
                    .mapNotNull { (_, list) -> list.maxByOrNull { it.height } }
                    .sortedByDescending { it.height }

                picked.forEach { src ->
                    callback.invoke(
                        newExtractorLink(
                            source = "Byse",
                            name = src.label,
                            url = src.url,
                            type = if (src.url.contains(".mp4", ignoreCase = true)) {
                                ExtractorLinkType.VIDEO
                            } else {
                                ExtractorLinkType.M3U8
                            }
                        ) {
                            this.referer = "https://sezonlukdizi.cc/"
                            this.quality = if (src.height > 0) src.height else Qualities.Unknown.value
                            this.headers = mapOf(
                                "User-Agent" to PLAYER_UA,
                                "Referer" to "https://sezonlukdizi.cc/"
                            )
                        }
                    )
                }
            }

            val tracks = plain.path("tracks")
            if (tracks.isArray) {
                tracks.forEach { track ->
                    val trackUrl = track.path("url").asText(null) ?: return@forEach
                    if (trackUrl.isBlank()) return@forEach
                    val subtitleUrl = if (trackUrl.startsWith("//")) "https:$trackUrl" else trackUrl
                    subtitleCallback.invoke(
                        SubtitleFile(
                            lang = track.path("language").asText("").ifBlank { track.path("title").asText("Subtitle") },
                            url = subtitleUrl
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun postJson(url: String, body: String, extraHeaders: Map<String, String> = emptyMap()): String? {
        val requestBody = body.toRequestBody("application/json".toMediaType())
        val headers = baseHeaders() + extraHeaders
        return app.post(url, requestBody = requestBody, headers = headers).text
    }

    private fun baseHeaders(): Map<String, String> = mapOf(
        "Accept" to "application/json",
        "Referer" to "https://sezonlukdizi.cc/",
        "Origin" to "https://sezonlukdizi.cc",
        "User-Agent" to PLAYER_UA
    )

    private fun jsonString(s: String): String = ObjectMapper().writeValueAsString(s)

    /** Decodes the AES-GCM playback payload; key = key_parts[version-1] || key_parts[30-version] (b64url). */
    private fun decryptPlayback(pb: JsonNode): JsonNode? {
        return try {
            val keyParts = pb.path("key_parts")
            if (!keyParts.isArray) return null
            val version = pb.path("version").asInt(0)
            val a = version          // 1-based index
            val b = 31 - version
            if (a < 1 || a > keyParts.size() || b < 1 || b > keyParts.size()) return null

            val keyRaw = b64UrlDecode(keyParts.get(a - 1).asText()) +
                b64UrlDecode(keyParts.get(b - 1).asText())
            if (keyRaw.size != 32) return null

            val iv = b64UrlDecode(pb.path("iv").asText())
            val payload = b64UrlDecode(pb.path("payload").asText())

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyRaw, "AES"), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(payload)

            ObjectMapper().readTree(String(plain, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }

    private fun b64UrlDecode(s: String): ByteArray {
        val fixed = s.replace('-', '+').replace('_', '/').trim()
        val padded = fixed + "=".repeat((4 - fixed.length % 4) % 4)
        return Base64.decode(padded, Base64.NO_WRAP)
    }

    // ── Proof of Work ─────────────────────────────────────────────────────
    // Port of the Byse frontend custom hash (pow bundle `gr` function).

    private fun rotl(x: Int, k: Int): Int = (x shl k) or (x ushr (32 - k))
    private fun imul(a: Int, b: Int): Int = (a.toLong() * b.toLong()).toInt()
    private fun mixRound(t: IntArray) {
        t[0] += t[1]; t[3] = rotl(t[3] xor t[0], 16); t[2] += t[3]; t[1] = rotl(t[1] xor t[2], 12)
        t[0] += t[1]; t[3] = rotl(t[3] xor t[0], 8); t[2] += t[3]; t[1] = rotl(t[1] xor t[2], 7)
    }

    /** Leading zero bits across the 8 words. */
    private fun zeroBits(n: IntArray): Int {
        var k = 0
        for (w in n) {
            if (w == 0) {
                k += 32
            } else {
                k += Integer.numberOfLeadingZeros(w)
                break
            }
        }
        return k
    }

    private fun grHash(data: ByteArray, length: Int, e: IntArray, table: IntArray, out: IntArray) {
        System.arraycopy(SEED, 0, e, 0, 4)
        for (idx in 0 until length) {
            e[0] += data[idx].toInt() and 0xFF
            e[0] = rotl(e[0], 7)
            mixRound(e)
        }
        repeat(8) { mixRound(e) }

        for (i in 0 until 512) {
            mixRound(e)
            table[i] = e[0] xor e[2]
        }
        repeat(2) {
            for (s in 0 until 512) {
                val a = table[s] and 511
                var c = table[s] + table[a]
                c = rotl(c, 13)
                c = c xor imul(table[(s + 1) and 511], -1640531535) // 2654435761 mod 2^32
                table[s] = c
                e[0] = e[0] xor c
                mixRound(e)
            }
        }

        for (i in 0 until 8) {
            mixRound(e)
            var s = e[0]
            val base = i * 64
            for (c in 0 until 64) {
                val d = table[base + c]
                s += d
                s = rotl(s, 5)
                s = s xor imul(d, -2048144777) // 2246822519 mod 2^32
            }
            out[i] = s xor e[2]
        }
    }

    private fun solvePow(nonce: String, difficulty: Int): String? {
        if (difficulty <= 0) return "0"
        val prefix = (nonce + ":").toByteArray(Charsets.ISO_8859_1)
        val prefixLen = prefix.size

        // Scratch buffers are reused across iterations: ~65k hashes per challenge would
        // otherwise allocate a 512-word table per attempt (~150 MB of garbage).
        val buffer = ByteArray(prefixLen + 16)
        System.arraycopy(prefix, 0, buffer, 0, prefixLen)
        val e = IntArray(4)
        val table = IntArray(512)
        val out = IntArray(8)

        val startedAt = System.currentTimeMillis()
        var i = 0
        while (System.currentTimeMillis() - startedAt < powTimeoutMs) {
            val len = prefixLen + writeInt(buffer, prefixLen, i)
            grHash(buffer, len, e, table, out)
            if (zeroBits(out) >= difficulty) return i.toString()
            i++
        }
        return null
    }

    /** Writes [value] as ASCII decimal digits into [target] at [offset], returns the digit count. */
    private fun writeInt(target: ByteArray, offset: Int, value: Int): Int {
        if (value == 0) {
            target[offset] = '0'.code.toByte()
            return 1
        }
        var digits = 0
        var v = value
        while (v > 0) {
            digits++
            v /= 10
        }
        var pos = offset + digits - 1
        v = value
        while (v > 0) {
            target[pos--] = ('0'.code + v % 10).toByte()
            v /= 10
        }
        return digits
    }

    private data class ByseSource(val mime: String, val url: String, val label: String, val height: Int)

    companion object {
        private const val PLAYER_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        /** 1779033703, 3144134277, 1013904242, 2773480762 (mod 2^32). */
        private val SEED = intArrayOf(1779033703, -1150833019, 1013904242, -1521486534)
    }
}
