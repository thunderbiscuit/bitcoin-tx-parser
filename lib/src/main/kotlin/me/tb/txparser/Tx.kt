/*
 * Copyright 2022-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package me.tb.txparser

import me.tb.txparser.txelements.FullTx
import me.tb.txparser.txelements.Input
import me.tb.txparser.txelements.Locktime
import me.tb.txparser.txelements.OutpointTxid
import me.tb.txparser.txelements.OutpointVout
import me.tb.txparser.txelements.Output
import me.tb.txparser.txelements.OutputAmount
import me.tb.txparser.txelements.ScriptPubKey
import me.tb.txparser.txelements.ScriptSig
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

typealias Witness = List<UByteArray>

class Tx(
    val rawTx: FullTx,
    val version: Version,
    val inputs: List<Input>,
    val outputs: List<Output>,
    val locktime: Locktime? = null,
    val segWit: Boolean
) {
    override fun toString(): String {
        return buildString {
            append("Transaction(")
            append("version=${version.value} ")
            append("segWit=$segWit ")
            append("inputs=${inputs.size} ")
            append("outputs=${outputs.size} ")
            append("locktime=$locktime)")
        }
    }

    companion object {
        fun fromRawTx(rawTx: UByteArray): Tx {
            val txReader = TxReader(rawTx)

            val version = Version(txReader.getNext(4))
            if (version.value != 2u) {
                throw IllegalArgumentException("Only transactions version 2 are supported by this library")
            }

            val inputs: List<Input> = parseInputs(txReader)

            // This is where you check for SegWit; the transaction has a 0x00 byte after the version
            // (implying 0 inputs), we know the transaction is signalling SegWit.
            if (inputs.isEmpty()) {
                val segwitFlag: UByte = txReader.getNext(1).first()
                if (segwitFlag != 0x01u.toUByte()) {
                    throw UnsupportedSegwitFlag()
                }
                val segwitInputs: List<Input> = parseInputs(txReader)
                val outputs: List<Output> = parseOutputs(txReader)
                val witnesses: List<Witness> = parseWitnesses(txReader, segwitInputs.size)
                val locktime = Locktime(txReader.getNext(4))

                segwitInputs.forEachIndexed{ index, input ->
                    input.witness = witnesses[index]
                }

                return Tx(
                    rawTx = FullTx(rawTx),
                    version = version,
                    inputs = segwitInputs,
                    outputs = outputs,
                    locktime = locktime,
                    segWit = true
                )
            }

            val outputs: List<Output> = parseOutputs(txReader)
            val locktime = Locktime(txReader.getNext(4))

            return Tx(
                rawTx = FullTx(rawTx),
                version = version,
                inputs = inputs,
                outputs = outputs,
                locktime = locktime,
                segWit = false
            )
        }

        // Give this function a TxReader and it will parse all your inputs
        // An input is made up of:
        //     - 32 bytes outpoint txid
        //     - 4 bytes outpoint vout
        //     - 1 to 9 bytes varint declaring size of scriptSig
        //     - variable number of bytes for scriptSig
        //     - 4 bytes sequence
        private fun parseInputs(txReader: TxReader): List<Input> {
            val inputList: MutableList<Input> = mutableListOf()
            val numInputsVarInt: VarInt = txReader.getNextVarint()
            val numInputs = numInputsVarInt.value

            repeat(numInputs) {
                val outPointTxid = txReader.getNext(32)
                val outPointVout = txReader.getNext(4)
                val scriptSigVarint: VarInt = txReader.getNextVarint()
                val scriptSig = txReader.getNext(scriptSigVarint.value)
                val sequence = txReader.getNext(4)

                inputList.add(
                    Input(
                        OutpointTxid(outPointTxid),
                        OutpointVout(outPointVout),
                        scriptSigVarint,
                        ScriptSig(scriptSig),
                        Sequence(sequence),
                        witness = null
                    )
                )
            }
            return inputList
        }

        // Give this function a TxReader and it will parse all your outputs
        // An output is made up of:
        //     - 8 bytes output amount
        //     - 1 to 9 bytes varint declaring size of scriptPubKey
        //     - variable number of bytes for scriptPubKey
        private fun parseOutputs(txReader: TxReader): List<Output> {
            val outputList: MutableList<Output> = mutableListOf()
            val numOutputsVarInt: VarInt = txReader.getNextVarint()
            val numOutputs = numOutputsVarInt.value

            repeat(numOutputs) {
                val outputAmount = txReader.getNext(8)
                val scriptPubKeyVarint: VarInt = txReader.getNextVarint()
                val scriptPubKey = txReader.getNext(scriptPubKeyVarint.value)

                outputList.add(
                    Output(
                        OutputAmount(outputAmount),
                        ScriptPubKey(scriptPubKey)
                    )
                )
            }
            return outputList
        }

        private fun parseWitnesses(txReader: TxReader, numWitnesses: Int): List<Witness> {
            val witnessesList: MutableList<List<UByteArray>> = mutableListOf()

            repeat(numWitnesses) {
                val witnessItems: MutableList<UByteArray> = mutableListOf()
                val numWitnessItems: Int = txReader.getNextVarint().value
                repeat(numWitnessItems) {
                    val witnessItemLength: Int = txReader.getNextVarint().value
                    val witnessItem = txReader.getNext(witnessItemLength)
                    witnessItems.add(witnessItem)
                }
                witnessesList.add(witnessItems)
            }
            return witnessesList
        }
    }
}
