/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser.txelements

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Version(
    override val bytes: UByteArray
): TxElement {
    val version: UInt

    init {
        require(bytes.size == 4) { "Version must be 4 bytes long" }
        val buffer = ByteBuffer.wrap(bytes.toByteArray())
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        version = buffer.int.toUInt()
    }
}
