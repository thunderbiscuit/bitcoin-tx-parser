/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import java.security.MessageDigest

fun doubleHashSha256(message: UByteArray): UByteArray {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(
            MessageDigest.getInstance("SHA-256").digest(message.toByteArray())
        ).toUByteArray()
}
