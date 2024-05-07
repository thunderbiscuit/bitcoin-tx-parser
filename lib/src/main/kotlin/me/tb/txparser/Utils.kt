/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import com.google.common.io.BaseEncoding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.MessageDigest
import org.bouncycastle.crypto.digests.RIPEMD160Digest

@OptIn(ExperimentalUnsignedTypes::class)
fun doubleHashSha256(message: UByteArray): UByteArray {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(
            MessageDigest.getInstance("SHA-256").digest(message.toByteArray())
        ).toUByteArray()
}

// OP_HASH160 is a SHA-256 hash wrapped in a RIPEMD-160 hash
// fun opHash160(message: ByteArray): ByteArray {
//     val sha256Hash = MessageDigest.getInstance("SHA-256").digest(message)
//     val digest = RIPEMD160Digest()
//     digest.update(sha256Hash, 0, sha256Hash.size)
//     val result = ByteArray(digest.digestSize)
//     digest.doFinal(result, 0)
//     return result
// }

// fun ByteArray.toHex(): String {
//     return BaseEncoding.base16().encode(this).lowercase()
// }

// must provide a hex in string format
// fun String.toByteArray(): ByteArray {
//     return BaseEncoding.base16().decode(this.uppercase())
// }

// // https://bitcoin.stackexchange.com/questions/44024/get-uncompressed-public-key-from-compressed-form
// fun uncompressECPoint(compressedPubKey: ByteArray): PointCoordinates {
//     val spec: ECParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
//     val point: ECPoint = spec.curve.decodePoint(compressedPubKey)
//     val x: ByteArray = point.xCoord.encoded
//     val y: ByteArray = point.yCoord.encoded
//     return PointCoordinates(x, y)
// }
//
// fun parseLocktime(bytes: ByteArray): String {
//     val value: Long = BigInteger(bytes.reversedArray()).toLong()
//     return if (value == 0L) {
//         "Locktime field: 0x${bytes.toHex()}, spendable right away"
//     } else if (value <= 500_000_000L) {
//         "Locktime field: 0x${bytes.toHex()}, spendable as per block $value"
//     } else {
//         "Locktime field: 0x${bytes.toHex()}, spendable as per Unix timestamp $value"
//     }
// }
//
// // fun validateP2PKHScript(scriptSig: ScriptSigP2PKH, scriptPubKey: ByteArray, ): Boolean {
// //
// //     return false
// // }
//
// // A P2PKH ScriptSig is <sig> <pubkey>
// data class ScriptSigP2PKH(val signature: ByteArray, val sigHash: SigHash, val rawPubKey: ByteArray)
//
// fun parseP2pkhScriptSig(scriptSig: ByteArray): ScriptSigP2PKH {
//     val opCode1 = parseOpCode(scriptSig[0])
//     if (opCode1 !is Opcode.PushBytes) throw Exception("This doesn't look like a P2PKH!")
//     val signature = scriptSig.copyOfRange(1, opCode1.numBytes)
//
//     val sigHash = parseSigHashByte(scriptSig[1 + signature.size])
//
//     val opCode2 = parseOpCode(scriptSig[1 + signature.size + 1])
//     if (opCode2 !is Opcode.PushBytes) throw Exception("This doesn't look like a P2PKH!")
//     // opcode + sig + sighash + opcode to
//     // opcode + sig + sighash + opcode + pubkey
//     val rawPubKey = scriptSig.copyOfRange(
//         1 + signature.size + 1 + 1,
//         1 + signature.size + 1 + 1 + opCode2.numBytes
//     )
//
//     println("rawPubKey is ${rawPubKey.toHex()}")
//     return ScriptSigP2PKH(signature, sigHash, rawPubKey)
// }
//
// suspend fun fetchScriptPubKey(outpoint: OutPoint): ByteArray {
//     val txid = outpoint.txid
//     val client = HttpClient(CIO)
//     val response: HttpResponse = client.get("https://blockstream.info/api/tx/${txid}/hex")
//     val txHex: String = response.body()
//     val rawTx = txHex.toByteArray()
//     val tx = Tx(rawTx)
//     return tx.getScriptPubKey(outpoint.vout.toInt())
// }
//
// data class PointCoordinates(val x: ByteArray, val y: ByteArray)
