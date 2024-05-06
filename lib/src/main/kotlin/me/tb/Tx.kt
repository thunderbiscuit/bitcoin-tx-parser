package me.tb

import kotlinx.coroutines.runBlocking
import java.lang.Long.parseLong
import java.math.BigInteger
import java.security.Signature

class Tx(rawTx: ByteArray) {

    private val txData: TxDataStructure
    init {
        txData = txParser(rawTx)
    }

    val numInputs: Int = txData.inputs.size
    val numOutputs: Int = txData.outputs.size
    val txid: String = doubleHashSha256(txData.rawTx.bytes).reversedArray().toHex()

    fun getScriptPubKey(outputNumber: Int): ByteArray {
        return txData.outputs[outputNumber].scriptPubKey.bytes
    }

    fun validateInputSig(inputNumber: Int): Boolean {

        val outpoint = OutPoint(
            txid = txid,
            vout = parseLong(txData.inputs[inputNumber].outPointVout.bytes.reversedArray().toHex(), 16)
        )

        var scriptPubKey: ByteArray = byteArrayOf()
        runBlocking {
            scriptPubKey = fetchScriptPubKey(outpoint)
        }

        val scriptSig = txData.inputs[inputNumber].scriptSig.bytes
        val (signature, sigHash, rawPubKey) = parseP2pkhScriptSig(scriptSig)

        // only working with SIGHASH_ALL for now
        if (sigHash != SigHash.SIGHASH_ALL) throw Exception("Cannot work with this sighash!")
        val txWithoutScriptSig = removeScriptSig(inputNumber)
        val message = txWithoutScriptSig.preScriptSig + scriptPubKey + txWithoutScriptSig.postScriptSig + sigHashTo4Bytes(SigHash.SIGHASH_ALL)
        val messageDigest = doubleHashSha256(message)

        if (rawPubKey[0] != 4.toByte()) {
            val uncompressedPubkeyCoordinates = uncompressECPoint(rawPubKey)
            val pubKey = PubKey(
                x = BigInteger(uncompressedPubkeyCoordinates.x),
                y = BigInteger(uncompressedPubkeyCoordinates.y),
            )
            val standardPublicKey = pubKey.pubKey
            val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
            ecdsaVerify.initVerify(standardPublicKey)
            ecdsaVerify.update(messageDigest)

            val result: Boolean = ecdsaVerify.verify(signature)
            println("Validating signature on input $inputNumber: $result")
            return result

        } else {
            val pubKey: PubKey = PubKey(
                x = BigInteger(rawPubKey.copyOfRange(1, 33)),
                y = BigInteger(rawPubKey.copyOfRange(33, 65))
            )
            val standardPublicKey = pubKey.pubKey
            val ecdsaVerify = Signature.getInstance("NONEwithECDSA")
            ecdsaVerify.initVerify(standardPublicKey)
            ecdsaVerify.update(messageDigest)

            val result: Boolean = ecdsaVerify.verify(signature)
            println("Validating signature on input $inputNumber: $result")
            return result
        }
    }

    private fun removeScriptSig(inputNumber: Int): TxWithoutScriptSig {
        val firstPart: ByteArray = generateBeforeScriptSig(inputNumber)
        val secondPart: ByteArray = generateAfterScriptSig(inputNumber)
        return TxWithoutScriptSig(firstPart, secondPart)
    }


    private fun generateBeforeScriptSig(inputNumber: Int): ByteArray {
        var result: ByteArray = byteArrayOf()
        result += txData.version.bytes
        result += txData.numInputsVarInt.bytes
        txData.inputs.forEachIndexed { index, data ->
            println("Working on index $index")
            if (index == inputNumber) {
                result += data.outPointTxid.bytes + data.outPointVout.bytes
                return result
            } else {
                data.fullInputBytes.bytes
            }
        }

        return result
    }

    private fun generateAfterScriptSig(inputNumber: Int): ByteArray {
        var result: ByteArray = byteArrayOf()
        txData.inputs.forEachIndexed { index, data ->
            if (index == inputNumber) {
                result += data.sequence.bytes
            }
            if (index > inputNumber) {
                result += data.fullInputBytes.bytes
            }
        }
        result += txData.numOutputsVarInt.bytes
        txData.outputs.forEach {
            result += it.fullOutputBytes.bytes
        }
        result += txData.locktime.bytes

        return result
    }
}

data class OutPoint(val txid: String, val vout: Long)
data class TxWithoutScriptSig(val preScriptSig: ByteArray, val postScriptSig: ByteArray)