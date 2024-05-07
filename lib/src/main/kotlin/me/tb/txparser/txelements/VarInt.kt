/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser.txelements

import java.math.BigInteger

// known bug: the max value here is kept as an Int
// whereas the value of the varint could in fact be above the max value for Int
@OptIn(ExperimentalStdlibApi::class)
class VarInt(fullNineBytes: UByteArray): TxElement {
    val value: Int
    val length: Int
    override val bytes: UByteArray
    private val firstByte: UByte = fullNineBytes.first()

    init {
        require (fullNineBytes.size == 9) { "VarInt constructor byte array must be 9 bytes long" }
        when (firstByte.toInt()) {
            in 0..<253 -> {
                value = BigInteger.valueOf(firstByte.toLong()).toInt()
                length = 1
                bytes = fullNineBytes.copyOfRange(0, 1)
            }
            253 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 3).toByteArray()).toInt()
                length = 2
                bytes = fullNineBytes.copyOfRange(0, 3)
            }
            254 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 5).toByteArray()).toInt()
                length = 4
                bytes = fullNineBytes.copyOfRange(0, 5)
            }
            255 -> {
                value = BigInteger(fullNineBytes.copyOfRange(1, 9).toByteArray()).toInt()
                length = 8
                bytes = fullNineBytes
            }
            else -> throw IllegalStateException()
        }
    }

    override fun toString(): String {
        return "VarInt(value=$value, length=$length, raw=${bytes.toHexString()})"
    }
}
