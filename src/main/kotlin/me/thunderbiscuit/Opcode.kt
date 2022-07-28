package me.thunderbiscuit

fun parseOpCode(byte: Byte): OpCode {
    println("Parsing OpCode: ${byte.toInt()}")
    return when (byte) {
        71.toByte() -> PushBytes(71)
        33.toByte() -> PushBytes(33)
        118.toByte() -> StandardOpCode.OP_DUP
        else -> throw IllegalStateException("OpCode not enabled yet")
    }
}

interface OpCode

enum class StandardOpCode: OpCode {
    OP_DUP
}

data class PushBytes(val numBytes: Int): OpCode
