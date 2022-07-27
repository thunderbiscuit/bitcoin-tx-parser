package me.thunderbiscuit

import com.google.common.io.BaseEncoding
import java.math.BigInteger
import java.security.MessageDigest

fun doubleHashSha256(message: String): ByteArray {
    val firstHash: ByteArray = MessageDigest.getInstance("SHA-256").digest(message.toByteArray())
    val secondHash: ByteArray = MessageDigest.getInstance("SHA-256").digest(firstHash)
    return secondHash
}

fun ByteArray.toHex(): String {
    return BaseEncoding.base16().encode(this).lowercase()
}

// a hex in string format
fun String.toByteArray(): ByteArray {
    return BaseEncoding.base16().decode(this.uppercase())
}

// known bug: because of the way we handle the parsing of the inputs and outputs
// and its requirement for a map of starting bytes and readability of
// this map and access to its components, the max value here is kept as an Int
// whereas the value of the varint could in fact be above the max value for Int
class VarInt(firstByte: Byte, fullNineBytes: ByteArray) {
    val value: Int
    val length: Int

    init {
        when (firstByte.toInt()) {
            in 0..253 -> {
                value = BigInteger.valueOf(firstByte.toLong()).toInt()
                length = 1
            }
            253 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 3)).toInt()
                length = 2
            }
            254 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 5)).toInt()
                length = 4
            }
            255 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 9)).toInt()
                length = 8
            }
            else -> throw IllegalStateException()
        }
    }
}
