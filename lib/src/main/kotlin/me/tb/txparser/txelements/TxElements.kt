/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser.txelements

sealed interface TxElement {
    val bytes: UByteArray
}

class FullTx(override val bytes: UByteArray): TxElement
class OutpointTxid(override val bytes: UByteArray): TxElement
class OutpointVout(override val bytes: UByteArray): TxElement
class ScriptSig(override val bytes: UByteArray): TxElement
class Sequence(override val bytes: UByteArray): TxElement
class OutputAmount(override val bytes: UByteArray): TxElement
class ScriptPubKey(override val bytes: UByteArray): TxElement
