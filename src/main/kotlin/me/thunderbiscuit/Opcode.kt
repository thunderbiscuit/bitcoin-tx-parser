package me.thunderbiscuit

fun parseOpCode(byte: Byte): Opcode {
    println("Parsing OpCode: ${byte.toInt()}")
    return when (byte) {
        71.toByte()  -> Opcode.PushBytes(71)
        33.toByte()  -> Opcode.PushBytes(33)
        118.toByte() -> Opcode.OP_DUP()
        else -> throw IllegalStateException("OpCode ${byte.toInt()} not enabled yet")
    }
}

sealed class Opcode() {
    class PushBytes(val numBytes: Int): Opcode()
    class OP_DUP: Opcode()
    class OP_HASH160(val bytes: ByteArray): Opcode()
}
