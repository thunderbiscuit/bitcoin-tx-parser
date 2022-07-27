package me.thunderbiscuit

import java.math.BigInteger

class Tx(hex: String) {
    private val rawTx: ByteArray = hex.toByteArray()
    val txid: String = doubleHashSha256(rawTx.toHex()).reversedArray().toHex()
    val txSize: Int = rawTx.size
    private val varintByteForInputs: Int = 4
    private var varintByteForOutputs: Int? = null
    private var startByteNLocktime: Int? = null

    // version
    val version: Int = BigInteger(rawTx.copyOfRange(0, varintByteForInputs).reversedArray()).toInt()

    // inputs
    private val inputsVarint: VarInt = VarInt(rawTx[varintByteForInputs], rawTx.copyOfRange(varintByteForInputs, varintByteForInputs + 9))
    private val startByteInputs: Int = varintByteForInputs + inputsVarint.length
    val numInputs: Int = inputsVarint.value
    val inputs: List<Input> = parseInputs()

    // outputs
    private val outputsVarint: VarInt = VarInt(rawTx[varintByteForOutputs!!], rawTx.copyOfRange(varintByteForOutputs!!, varintByteForOutputs!! + 9))
    private var startByteOutputs: Int = varintByteForOutputs!! + outputsVarint.length
    val numOutputs: Int = outputsVarint.value
    val outputs: List<Output> = parseOutputs()

    // locktime
    val locktime: String = parseLocktime(rawTx.copyOfRange(startByteNLocktime!!, startByteNLocktime!! + 4))

    private fun parseInputs(): List<Input> {
        val list: MutableList<Input> = mutableListOf()
        val inputsStartByte: Map<Int, Int> = buildStartByteInputsMap()

        for (i: Int in 0 until numInputs) {
            val txidStart = inputsStartByte[i]!!
            val txidEnd = txidStart + 32

            val voutStart = txidEnd
            val voutEnd = txidEnd + 4

            val varInt = VarInt(rawTx[inputsStartByte[i]!! + 36], rawTx.copyOfRange(inputsStartByte[i]!! + 36, inputsStartByte[i]!! + 44))

            val scriptSigStart = voutEnd + varInt.length
            val scriptSigEnd = scriptSigStart + varInt.value

            val sequenceStart = scriptSigEnd
            val sequenceEnd = sequenceStart + 4

            // if we're on the last loop, we now know the first byte of the outputs
            if (i == numInputs - 1) varintByteForOutputs = sequenceEnd

            val txid = rawTx.copyOfRange(txidStart, txidEnd).reversedArray().toHex()
            val vout = BigInteger(rawTx.copyOfRange(voutStart, voutEnd).reversedArray()).toInt()
            val scriptSig = rawTx.copyOfRange(scriptSigStart, scriptSigEnd).toHex()
            val sequence = rawTx.copyOfRange(sequenceStart, sequenceEnd).reversedArray().toHex()
            val input = Input(
                OutPoint(txid, vout),
                scriptSig,
                sequence
            )

            list.add(i, input)
        }
        return list
    }

    private fun buildStartByteInputsMap(): Map<Int, Int> {
        val map: MutableMap<Int, Int> = mutableMapOf()
        for (i: Int in 0 until numInputs) {
            if (i == 0) {
                map.put(0, startByteInputs)
            } else {
                map.put(i, calculateNextInputStart(map[i-1]!!))
            }
        }
        println(map)
        return map
    }

    private fun calculateNextInputStart(previous: Int): Int {
        val varInt = VarInt(rawTx[previous + 36], rawTx.copyOfRange(previous + 36, previous + 44))
        val scriptSigLength: Int = varInt.value

        // previous start byte
        //     + 32 for outpoint txid
        //     + 4 for output number
        //     + length of varint for script length
        //     + script length
        //     + 4 for sequence
        return previous + 4 + 32 + varInt.length + scriptSigLength + 4
    }

    private fun parseOutputs(): List<Output> {
        println("Number of outputs: $numOutputs")
        val list: MutableList<Output> = mutableListOf()
        val outputsStartByte: Map<Int, Int> = buildStartByteOutputMap()

        for (i: Int in 0 until numOutputs) {
            val amountStart: Int = outputsStartByte[i]!!
            val amountEnd: Int = amountStart + 8

            val varInt = VarInt(
                firstByte = rawTx[outputsStartByte[i]!! + 8],
                fullNineBytes = rawTx.copyOfRange(outputsStartByte[i]!! + 8, outputsStartByte[i]!! + 16)
            )

            val scriptPubkeyStart: Int = amountEnd + varInt.length
            val scriptPubkeyEnd: Int = scriptPubkeyStart + varInt.value

            // if we're on the last loop, we now know the first byte of the nLocktime
            if (i == numOutputs - 1) startByteNLocktime = scriptPubkeyEnd

            val amount: Long = BigInteger(rawTx.copyOfRange(amountStart, amountEnd).reversedArray()).toLong()
            val scriptPubkey: String = rawTx.copyOfRange(scriptPubkeyStart, scriptPubkeyEnd).toHex()

            list.add(Output(amount, scriptPubkey))
        }
        return list
    }

    private fun buildStartByteOutputMap(): Map<Int, Int> {
        val map: MutableMap<Int, Int> = mutableMapOf()
        for (i: Int in 0 until numOutputs) {
            if (i == 0) {
                map.put(0, startByteOutputs)
            } else {
                map.put(i, calculateNextOutputStart(map[i-1]!!))
            }
        }
        println(map)
        return map
    }

    private fun calculateNextOutputStart(previous: Int): Int {
        val varInt = VarInt(rawTx[previous + 8], rawTx.copyOfRange(previous + 8, previous + 16))
        val scriptPubkeyLength: Int = varInt.value

        // previous start byte
        //     + 8 for amount
        //     + length of varint for scriptPubkey length
        //     + length of scriptPubkey
        return previous + 8 + varInt.length + scriptPubkeyLength
    }

    private fun parseLocktime(bytes: ByteArray): String {
        val value: Long = BigInteger(bytes.reversedArray()).toLong()
        return if (value == 0L) {
            "Locktime field: 0x${bytes.toHex()}, spendable right away"
        } else if (value <= 500_000_000L) {
            "Locktime field: 0x${bytes.toHex()}, spendable as per block $value"
        } else {
            "Locktime field: 0x${bytes.toHex()}, spendable as per Unix timestamp $value"
        }
    }
}

data class Input(val outPoint: OutPoint, val scriptSig: String, val sequence: String)
data class OutPoint(val txid: String, val vout: Int)
data class Output(val amount: Long, val scriptPubkey: String)
