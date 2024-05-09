package me.tb.txparser.txelements

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class Locktime {
    data class Blocks(
        val height: UInt,
        val bytes: UByteArray
    ) : Locktime()

    data class Time(
        val timestamp: UInt,
        val bytes: UByteArray
    ) : Locktime()

    companion object {
        fun fromRaw(bytes: UByteArray): Locktime {
            require(bytes.size == 4) { "Locktime field must be 4 bytes" }
            val buffer = ByteBuffer.wrap(bytes.toByteArray())
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            val number = buffer.int.toUInt()

            return when {
                number < 500_000_000u -> Blocks(number, bytes)
                else -> Time(number, bytes)
            }
        }
    }
}
