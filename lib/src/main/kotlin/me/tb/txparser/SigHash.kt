/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

enum class SigHash {
    SIGHASH_ALL
}

fun parseSigHashByte(byte: Byte): SigHash {
    return when (byte) {
        1.toByte() -> SigHash.SIGHASH_ALL
        else       -> throw Exception("We don't know this sighash type! (${byte.toInt()})")
    }
}

fun sigHashTo4Bytes(sigHash: SigHash): ByteArray {
    return when (sigHash) {
        SigHash.SIGHASH_ALL -> byteArrayOf(0x01, 0x00, 0x00, 0x00)
    }
}
