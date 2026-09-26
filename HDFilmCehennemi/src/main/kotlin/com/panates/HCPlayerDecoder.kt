package com.panates

import android.util.Base64

fun hdcDecodePlayer(payload: String, separator: String): String? {
    return try {
        val raw = payload.split(separator)
        if (raw.size < 3) return null
        val chunks = raw.toMutableList()
        val n = chunks.size - 2
        val i1 = n % 7
        val i2 = 8 + (n % 5)
        if (i1 >= chunks.size || i2 >= chunks.size) return null
        val chunkA = chunks.removeAt(i2)
        val chunkB = chunks.removeAt(i1)
        var s = chunks.joinToString("")
        if (chunkA.length > 2048) s = s.reversed()
        var k = 0
        var z = 0
        for (i in chunkB.indices) {
            val c = chunkB[i].code
            k = (k * 37 + c) % 241
            z = (z + ((c shl 1) xor i)) and 255
        }
        val seed = (k * 3 + z) % 256
        val step = (z % 11) + 5
        var lcg = ((z * 251 + k) % 65519) + 1
        for (i in chunkA.length - 1 downTo 0) {
            val ch = chunkA[i]
            when (ch) {
                '7' -> s = hdcAtob(s)
                '3' -> s = s.reversed()
                else -> {
                    val d = (26 - ((ch.code - 96) % 26)) % 26
                    val rotated = CharArray(s.length)
                    for (j in s.indices) {
                        val c = s[j]
                        rotated[j] = if (c in 'A'..'Z' || c in 'a'..'z') {
                            val base = if (c <= 'Z') 65 else 97
                            ((c.code - base + d) % 26 + base).toChar()
                        } else c
                    }
                    s = String(rotated)
                }
            }
        }
        if (chunkB.length > 4096) s = hdcAtob(s)
        val len = s.length
        if (len < 2) return null
        val swaps = IntArray(len)
        for (i in len - 1 downTo 1) {
            lcg = (lcg * 97 + 41) % 65519
            swaps[i] = lcg % (i + 1)
        }
        val chars = s.toCharArray()
        for (i in 1 until len) {
            val j = swaps[i]
            val tmp = chars[i]
            chars[i] = chars[j]
            chars[j] = tmp
        }
        var x = seed
        val out = StringBuilder(chars.size)
        for (c in chars) {
            x = (x * 5 + step) % 256
            out.append((c.code xor x).toChar())
            x = (x + c.code) % 256
        }
        out.toString()
    } catch (e: Throwable) {
        null
    }
}

internal fun hdcAtob(value: String): String {
    val bytes = Base64.decode(value, Base64.DEFAULT)
    return String(bytes, Charsets.ISO_8859_1)
}

internal fun hdcPlausibleStreamUrl(url: String): Boolean =
    url.startsWith("http") && url.length < 4096 && url.none { it.isWhitespace() }

internal fun hdcSlugFromEmbedUrl(url: String): String? =
    Regex("""/(?:embed|rplayer)/([^/?#]+)""").find(url)?.groupValues?.get(1)

private val HDC_SPLIT_CALL =
    Regex("""=\s*[A-Za-z0-9_$]+\("([^"]*)"\.split\("([^"]+)"\)\)""")

internal fun hdcSelectStreamUrl(text: String, slug: String?): String? {
    var fallback: String? = null
    for (match in HDC_SPLIT_CALL.findAll(text)) {
        val payload = match.groupValues[1].replace("\\/", "/")
        val separator = match.groupValues[2]
        if (payload.isEmpty() || separator.isEmpty()) continue
        val url = hdcDecodePlayer(payload, separator) ?: continue
        if (!hdcPlausibleStreamUrl(url)) continue
        if (slug != null && url.contains(slug)) return url
        if (fallback == null) fallback = url
    }
    return fallback
}
