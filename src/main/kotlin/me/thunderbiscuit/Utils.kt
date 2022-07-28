package me.thunderbiscuit

import com.google.common.io.BaseEncoding
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.MessageDigest
import java.security.Signature

fun doubleHashSha256(message: ByteArray): ByteArray {
    val firstHash: ByteArray = MessageDigest.getInstance("SHA-256").digest(message)
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

fun verifySignature(
    messageDigest: ByteArray,
    rawPubkey: ByteArray,
    signature: ByteArray,
    print: String,
): Boolean {
    // if (rawPubkey[0] != 4.toByte()) throw Exception("We're not able to parse compressed public keys yet!")
    println("verifySignature data: ${signature.toHex()}")

    if (rawPubkey[0] != 4.toByte()) {
        val uncompressedPubkeyCoordinates = uncompressECPoint(rawPubkey)
        val pubKey = PubKey(
            x = BigInteger(uncompressedPubkeyCoordinates.x),
            y = BigInteger(uncompressedPubkeyCoordinates.y),
        )
        val standardPublicKey = pubKey.pubKey
        val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
        ecdsaVerify.initVerify(standardPublicKey)

        // maybe needs to be reversed?
        // ecdsaVerify.update(messageDigest.reversedArray())
        ecdsaVerify.update(messageDigest)

        val result: Boolean = ecdsaVerify.verify(signature)
        println("$print $result")
        return result

    } else {
        val pubKey: PubKey = PubKey(
            x = BigInteger(rawPubkey.copyOfRange(1, 33)),
            y = BigInteger(rawPubkey.copyOfRange(33, 65))
        )
        val standardPublicKey = pubKey.pubKey
        val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
        ecdsaVerify.initVerify(standardPublicKey)
        // maybe needs to be reversed?
        // ecdsaVerify.update(messageDigest.reversedArray())
        ecdsaVerify.update(messageDigest)

        val result: Boolean = ecdsaVerify.verify(signature)
        println("$print $result")
        return result
    }

    // val standardPublicKey = pubKey.pubKey
    // val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
    // ecdsaVerify.initVerify(standardPublicKey)
    // ecdsaVerify.update(messageDigest)
    //
    // return ecdsaVerify.verify(signature)
}

// https://bitcoin.stackexchange.com/questions/44024/get-uncompressed-public-key-from-compressed-form
fun uncompressECPoint(compressedPubKey: ByteArray): PointCoordinates {
    val spec: ECParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
    val point: ECPoint = spec.curve.decodePoint(compressedPubKey)
    val x: ByteArray = point.xCoord.encoded
    val y: ByteArray = point.yCoord.encoded
    return PointCoordinates(x, y)
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

data class PointCoordinates(val x: ByteArray, val y: ByteArray)
