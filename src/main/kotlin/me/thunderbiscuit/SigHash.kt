package me.thunderbiscuit

enum class SigHash {
    SIGHASH_ALL
}

fun parseSigHashByte(byte: Byte): SigHash {
    return when (byte) {
        1.toByte() -> SigHash.SIGHASH_ALL
        else       -> throw Exception("We don't know this sighash type!")
    }
}

fun sigHashTo4Bytes(sigHash: SigHash): ByteArray {
    return when (sigHash) {
        SigHash.SIGHASH_ALL -> byteArrayOf(0x01, 0x00, 0x00, 0x00)
    }
}
