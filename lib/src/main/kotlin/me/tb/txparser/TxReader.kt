/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import me.tb.txparser.txelements.VarInt

class TxReader(private var rawTx: UByteArray) {
    fun get(): UByteArray {
        return rawTx
    }

    fun getNext(length: Int): UByteArray {
        if (length > rawTx.size) {
            throw IllegalArgumentException("Requested length $length exceeds remaining transaction data ${rawTx.size}")
        }
        val data = rawTx.copyOfRange(0, length)
        rawTx = rawTx.copyOfRange(length, rawTx.size)
        return data
    }

    fun getNextVarint(): VarInt {
        val varint = VarInt(rawTx.copyOfRange(0, 9))
        rawTx = rawTx.copyOfRange(varint.length, rawTx.size)
        return varint
    }

    fun isEmpty(): Boolean {
        return rawTx.isEmpty()
    }
}
