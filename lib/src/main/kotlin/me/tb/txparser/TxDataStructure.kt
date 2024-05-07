/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import me.tb.txparser.txelements.FullInputBytes
import me.tb.txparser.txelements.FullOutputBytes
import me.tb.txparser.txelements.FullTx
import me.tb.txparser.txelements.Locktime
import me.tb.txparser.txelements.OutpointTxid
import me.tb.txparser.txelements.OutpointVout
import me.tb.txparser.txelements.OutputAmount
import me.tb.txparser.txelements.ScriptPubKey
import me.tb.txparser.txelements.ScriptSig
import me.tb.txparser.txelements.ScriptSigVarInt
import me.tb.txparser.txelements.Sequence
import me.tb.txparser.txelements.VarInt
import me.tb.txparser.txelements.Version

// A transaction has the following fields:
// 1. version
// 2. inputs list
//   2a outpoint txid
//   2b outpoint input number
//   2c scriptSig
//   2d sequence
// 3. outputs list
//   3a amount
//   3b scriptPubKey
// 4. locktime

@OptIn(ExperimentalUnsignedTypes::class)
class TxDataStructure(
    val rawTx: FullTx,
    val version: Version,
    val inputs: List<Input>,
    val outputs: List<Output>,
    val locktime: Locktime
) {
    companion object {
        fun fromRawTx(rawTx: UByteArray): Unit {
            // We'll be using the idea of a "Next Interesting Byte Index" which is too long to read, so I'm renaming it
            // to nibi
            var nibi: Int = 0
            val txReader = TxReader(rawTx)

            // The version is always 4 bytes
            val versionBytes: Version = Version(txReader.getNext(4))
            // Update next interesting byte to varint for number of inputs
            // nibi += 4
            // val nextChunk = rawTx.copyOfRange(nibi, rawTx.size)
            //
            // val inputs: List<Input> = parseInputs(nextChunk)
            //
            // // update next interesting byte to varint for number of outputs
            // val sizeOfAllInputs = inputs.calculateInputsSize()
            // val inputVarint = VarInt(rawTx[nibi], rawTx.copyOfRange(nibi, nibi + 9))
            // nibi += inputVarint.length + sizeOfAllInputs
            //
            // val outputs: List<Output> = parseOutputs(nibi, rawTx)
            // // update next interesting byte to locktime
            // val outputVarint = VarInt(rawTx[nibi], rawTx.copyOfRange(nibi, nibi + 9))
            // val sizeOfAllOutputs = calculateOutputsSize(outputs)
            // nibi += outputVarint.length + sizeOfAllOutputs
            //
            // val locktimeBytes: ByteArray = rawTx.copyOfRange(nibi, nibi + 4)
            //
            // return TxDataStructure(
            //     rawTx = FullTx(rawTx),
            //     version = Version(versionBytes),
            //     inputs = inputs,
            //     outputs = outputs,
            //     locktime = Locktime(locktimeBytes)
            // )
        }
    }
}

data class Input(
    val fullInputBytes: FullInputBytes,
    val outPointTxid: OutpointTxid,
    val outPointVout: OutpointVout,
    val scriptSigVarInt: ScriptSigVarInt,
    val scriptSig: ScriptSig,
    val sequence: Sequence,
)

data class Output(
    val fullOutputBytes: FullOutputBytes,
    val outputAmount: OutputAmount,
    val scriptPubKey: ScriptPubKey,
)

// @OptIn(ExperimentalUnsignedTypes::class)
// class InputBookend(txChunk: UByteArray) {
//     // the input is made up of:
//     //     32 bytes outpoint txid
//     //     4 bytes outpoint vout
//     //     1 to 9 bytes varint declaring size of scriptSig
//     //     variable number of bytes for scriptSig
//     //     4 bytes sequence
//     private val scriptSigVarint = VarInt(txChunk.copyOfRange(36, 36 + 9))
//     val start = 0
//     val end = 32 + 4 + scriptSigVarint.length + scriptSigVarint.value + 4
// }

// class OutputBookend(startIndex: Int, rawTx: ByteArray) {
//     // the output is made up of:
//     //     8 bytes amount
//     //     1 to 9 bytes varint declaring size of scriptPubKey
//     //     variable number of bytes for scriptSig
//     private val scriptPubKeyVarint = VarInt(rawTx[startIndex + 8], rawTx.copyOfRange(startIndex + 8, startIndex + 8 + 9))
//     val start = startIndex
//     val end = startIndex + 8 + scriptPubKeyVarint.length + scriptPubKeyVarint.value
// }

// Give this function a starting point and it will parse all your inputs
// @OptIn(ExperimentalUnsignedTypes::class)
// fun parseInputs(txChunk: UByteArray): List<Input> {
//     val inputList: MutableList<Input> = mutableListOf()
//     val inputsVarint = VarInt(txChunk.toUByteArray().copyOfRange(0, 9))
//     val numInputs = inputsVarint.value
//     val bookends = createInputBookends(inputsVarint.length, rawTx, numInputs)
//
//     bookends.forEach {
//         val fullInputBytes = rawTx.copyOfRange(it.start, it.end)
//         val outpointTxid = rawTx.copyOfRange(it.start, it.start + 32)
//         val outpointVout = rawTx.copyOfRange(it.start + 32, it.start + 32 + 4)
//         val lengthOfScriptSigVarint = VarInt(rawTx[it.start + 32 + 4], rawTx.copyOfRange(it.start + 32 + 4, it.start + 32 + 4 + 9))
//         val scriptSig = rawTx.copyOfRange(
//             it.start + 32 + 4 + lengthOfScriptSigVarint.length,
//             it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value
//         )
//         val sequence = rawTx.copyOfRange(
//             it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value,
//             it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value + 4
//         )
//
//         inputList.add(
//             Input(
//                 FullInputBytes(fullInputBytes),
//                 OutpointTxid(outpointTxid),
//                 OutpointVout(outpointVout),
//                 ScriptSigVarInt(lengthOfScriptSigVarint.raw),
//                 ScriptSig(scriptSig),
//                 Sequence(sequence)
//             )
//         )
//     }

    // return inputList
// }

// fun createInputBookends(rawTx: ByteArray, numInputs: Int): List<InputBookend> {
//     val bookendList: MutableList<InputBookend> = mutableListOf()
//     for (i: Int in 0 until numInputs) {
//         if (i == 0) {
//             val bookend = InputBookend(txChunk)
//             bookendList.add(bookend)
//         } else {
//             val bookend = InputBookend(bookendList[i - 1].end, rawTx)
//             bookendList.add(bookend)
//         }
//     }
//
//     return bookendList
// }

// give this function a starting point and it will parse all your outputs
// fun parseOutputs(startIndex: Int, rawTx: ByteArray): List<Output> {
//     val outputList: MutableList<Output> = mutableListOf()
//     val outputVarint = VarInt(rawTx[startIndex], rawTx.copyOfRange(startIndex, startIndex + 9))
//     val numOutputs = outputVarint.value
//     val bookends = createOutputBookends(startIndex + outputVarint.length, rawTx, numOutputs)
//
//     bookends.forEach {
//         val fullOutputBytes = rawTx.copyOfRange(it.start, it.end)
//         val outputAmount = rawTx.copyOfRange(it.start, it.start + 8)
//         val lengthOfScriptPubKeyVarint = VarInt(rawTx[it.start + 8], rawTx.copyOfRange(it.start + 8, it.start + 8 + 9))
//         val scriptPubKey = rawTx.copyOfRange(
//             it.start + 8,
//             it.start + 8 + lengthOfScriptPubKeyVarint.length + lengthOfScriptPubKeyVarint.value
//         )
//
//         outputList.add(
//             Output(
//                 FullOutputBytes(fullOutputBytes),
//                 OutputAmount(outputAmount),
//                 ScriptPubKey(scriptPubKey)
//             )
//         )
//     }
//
//     return outputList
// }

// fun createOutputBookends(startIndex: Int, rawTx: ByteArray, numOutputs: Int): List<OutputBookend> {
//     val bookendList: MutableList<OutputBookend> = mutableListOf()
//     for (i: Int in 0 until numOutputs) {
//         if (i == 0) {
//             val bookend = OutputBookend(startIndex, rawTx)
//             bookendList.add(bookend)
//         } else {
//             val bookend = OutputBookend(bookendList[i - 1].end, rawTx)
//             bookendList.add(bookend)
//         }
//     }
//
//     return bookendList
// }

fun List<Input>.calculateInputsSize(): Int {
    var totalSize: Int = 0
    this.forEach {
        totalSize += it.fullInputBytes.bytes.size
    }
    return totalSize
}

fun calculateOutputsSize(outputs: List<Output>): Int {
    var totalSize: Int = 0
    outputs.forEach {
        totalSize += it.fullOutputBytes.bytes.size
    }
    return totalSize
}
