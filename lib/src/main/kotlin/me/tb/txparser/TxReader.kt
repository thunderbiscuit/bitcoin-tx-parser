package me.tb.txparser

import me.tb.txparser.txelements.VarInt

@OptIn(ExperimentalUnsignedTypes::class)
class TxReader(private var rawTx: UByteArray) {
    fun get(): UByteArray {
        return rawTx
    }

    fun getNext(length: Int): UByteArray {
        if (length > rawTx.size) {
            throw IllegalArgumentException("Requested length exceeds remaining transaction data")
        }

        rawTx = rawTx.copyOfRange(length, rawTx.size)
        return rawTx.copyOfRange(0, length)
    }

    fun getNextVarint(): VarInt {
        val varint = VarInt(rawTx.copyOfRange(0, 9))
        rawTx = rawTx.copyOfRange(varint.length, rawTx.size)
        return varint
    }

    fun remove(length: Int) {
        rawTx = rawTx.copyOfRange(length, rawTx.size)
    }
}
