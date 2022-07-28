package me.thunderbiscuit

import java.math.BigInteger

class Tx(hex: String) {
    private val rawTx: ByteArray = hex.toByteArray()
    val txid: String = doubleHashSha256(rawTx).reversedArray().toHex()
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
            println("Length of scriptSig for input $i: ${varInt.value}")

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

    fun validateTransaction(): Boolean {
        val inputsScriptPubkeys: MutableMap<Int, ByteArray> = mutableMapOf()
        // for (i: Int in 0 until numInputs) {
        //     inputsScriptPubkeys[i] = getInputScriptPubkey(inputs[i].outPoint)
        // }

        for (i: Int in 0 until numInputs) {
            println("Validating input $i")
            // only parses P2PKH
            val scriptSigData = parseScriptSig(inputs[i].scriptSig.toByteArray())
            println("ScriptSig pubkey is ${scriptSigData.pubkey.toHex()}")
            // if (scriptSigData.pubkey.size < 63) throw Exception("Currently unable to parse compressed public keys")

            val dataToRemoveStart: Int = 4 + inputsVarint.length + 32 + 4
            val varInt = VarInt(rawTx[dataToRemoveStart], rawTx.copyOfRange(dataToRemoveStart, dataToRemoveStart + 9))
            val dataToRemoveEnd: Int = dataToRemoveStart + varInt.length + varInt.value

            // signature validation requires a message and a pubkey
            val message: ByteArray = generateMessage(
                dataToRemoveStart = dataToRemoveStart,
                dataToRemoveEnd = dataToRemoveEnd,
                sigHash = scriptSigData.sigHash,
                scriptPubKey = scriptSigData.pubkey
            )
            val messageDigest: ByteArray = doubleHashSha256(message)
            verifySignature(
                messageDigest = messageDigest,
                rawPubkey = scriptSigData.pubkey,
                signature = scriptSigData.signature,
                print = "Signature on input $i for $txid is"
            )
        }

        println(inputsScriptPubkeys[0]?.toHex())
        return true
    }

    private fun generateMessage(
        dataToRemoveStart: Int,
        dataToRemoveEnd: Int,
        sigHash: SigHash,
        scriptPubKey: ByteArray
    ): ByteArray {
        val tx: ByteArray = rawTx
        // Step 1: remove the scriptSig, including the varint that specifies the length of the scriptSig
        // Step 2: replace it with the scriptPubkey in the parent tx
        // Step 3: append sighash value in 8 bytes
        val piece1: ByteArray = tx.copyOfRange(0, dataToRemoveStart)
        val piece2: ByteArray = tx.copyOfRange(dataToRemoveEnd, tx.size)
        val scriptPubKeyFromParent = "1976a9147b1b3760657d2919b2509acd653533efb847f3dd88ac".toByteArray()
        // val scriptPubKeyFromParent = "76a9147b1b3760657d2919b2509acd653533efb847f3dd88ac".toByteArray().reversedArray()
        val fullMessage: ByteArray = piece1 + scriptPubKeyFromParent + piece2 + sigHashTo4Bytes(sigHash)
        println("The message hashed and passed to the signature verification is ${fullMessage.toHex()}")
        return fullMessage
    }

    // this method only works when attempting to parse P2PKH ScriptSigs
    private fun parseScriptSig(rawScriptSig: ByteArray): ScriptSig {
        println("ScriptSig is ${rawScriptSig.toHex()}")
        // P2PKH scriptSig is <sig> <pubKey>
        val sigOpCodeByteIndex: Int = 0
        val sigOpCode: OpCode = parseOpCode(rawScriptSig[sigOpCodeByteIndex])
        val pubkeyOpCodeByteIndex: Int = if (sigOpCode is PushBytes) sigOpCode.numBytes + 1 else throw Exception("The first opcode was not PUSH_BYTES!")
        val pubkeyOpCode: OpCode = parseOpCode(rawScriptSig[pubkeyOpCodeByteIndex])

        val signatureWithSigHash: ByteArray = getRawSignature(sigOpCode, rawScriptSig)
        val signature: ByteArray = signatureWithSigHash.copyOfRange(0, signatureWithSigHash.size - 1)
        val rawPubkey: ByteArray = getRawPubkey(
            opCode = pubkeyOpCode,
            startByteIndex = pubkeyOpCodeByteIndex + 1,
            rawScriptSig = rawScriptSig
        )

        val sigHashByte: Byte = signatureWithSigHash.last()
        val sigHash: SigHash = parseSigHashByte(sigHashByte)
        // println("SigHash type is ${parseSigHashByte(sigHash)}")

        val scriptSig: ScriptSig = ScriptSig(signature, sigHash, rawPubkey)
        println("ScriptSig: $scriptSig")
        return scriptSig
    }

    private fun getRawPubkey(opCode: OpCode, startByteIndex: Int, rawScriptSig: ByteArray): ByteArray {
        println("getRawPubkey data: $opCode, $startByteIndex, ${rawScriptSig.size}")
        if (opCode is PushBytes) {
            // we need to add 1 to those calculations to account for the OpCode byte
            return rawScriptSig.copyOfRange(startByteIndex, startByteIndex + opCode.numBytes)
        } else {
            throw Exception("Script probably not P2PKH!")
        }
    }

    private fun getRawSignature(opCode: OpCode, rawScriptSig: ByteArray): ByteArray {
        if (opCode is PushBytes) {
            // we need to add 1 to those calculations to account for the OpCode byte
            return rawScriptSig.copyOfRange(1, opCode.numBytes + 1)
        } else {
            throw Exception("Script probably not P2PKH!")
        }
    }

    private fun getInputScriptPubkey(outPoint: OutPoint): ByteArray {
        // query blockstream API to get full hex of parent transaction

        val parentTx: Tx = Tx(hexTx4Parent)
        val scriptPubkey: ByteArray = parentTx.outputs[outPoint.vout].scriptPubkey.toByteArray()
        return scriptPubkey
    }
}

data class Input(val outPoint: OutPoint, val scriptSig: String, val sequence: String)
data class OutPoint(val txid: String, val vout: Int)
data class Output(val amount: Long, val scriptPubkey: String)
data class ScriptSig(val signature: ByteArray, val sigHash: SigHash, val pubkey: ByteArray)
