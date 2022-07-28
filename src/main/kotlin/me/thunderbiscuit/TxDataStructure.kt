package me.thunderbiscuit

// A transaction has the following fields:
// 1. version
// 2. inputs list
//   2.a all input bytes
//   2.b outpoint txid
//   2.c outpoint input number
//   2.d scriptSig
//   2.e sequence
// 3. outputs list
//   3.a all output bytes
//   3.b amount
//   3.c scriptPubkey
// 4. locktime

// the txParser() should return a TxDataStructure
fun txParser(rawTx: ByteArray): TxDataStructure {
    // we'll be using the idea of a "Next Interesting Byte Index"
    // which is too long to read, so I'm
    // renaming nextInterestingByteIndex to nibi
    var nibi: Int = 0

    val versionBytes: ByteArray = rawTx.copyOfRange(0, 4)
    // update next interesting byte to varint for number of inputs
    nibi += 4

    val inputs: List<InputData> = parseInputs(nibi, rawTx)
    // update next interesting byte to varint for number of outputs
    val inputVarint = VarInt(rawTx[nibi], rawTx.copyOfRange(nibi, nibi + 9))
    val sizeOfAllInputs = calculateInputsSize(inputs)
    nibi += inputVarint.length + sizeOfAllInputs

    val outputs: List<OutputData> = parseOutputs(nibi, rawTx)
    // update next interesting byte to locktime
    val outputVarint = VarInt(rawTx[nibi], rawTx.copyOfRange(nibi, nibi + 9))
    val sizeOfAllOutputs = calculateOutputsSize(outputs)
    nibi += outputVarint.length + sizeOfAllOutputs

    val locktimeBytes: ByteArray = rawTx.copyOfRange(nibi, nibi + 4)

    return TxDataStructure(
        rawTx = TxElement.FullTx(rawTx),
        version = TxElement.Version(versionBytes),
        inputs = inputs,
        outputs = outputs,
        locktime = TxElement.Locktime(locktimeBytes)
    )
}

class TxDataStructure(
    val rawTx: TxElement.FullTx,
    val version: TxElement.Version,
    val inputs: List<InputData>,
    val outputs: List<OutputData>,
    val locktime: TxElement.Locktime
) {
    val numInputs: Int = inputs.size
    val numOutputs: Int = outputs.size
    val txid: String = doubleHashSha256(rawTx.bytes).reversedArray().toHex()

    fun getScriptSig(inputNumber: Int): ByteArray {
        return inputs[inputNumber].scriptSig.bytes
    }

    fun getScriptPubKey(outputNumber: Int): ByteArray {
        return outputs[outputNumber].scriptPubKey.bytes
    }
}

data class InputData(
    val fullInputBytes: TxElement.FullInputBytes,
    val outPointTxid: TxElement.OutpointTxid,
    val outPointVout: TxElement.OutpointVout,
    val scriptSig: TxElement.ScriptSig,
    val sequence: TxElement.Sequence,
)

data class OutputData(
    val fullOutputBytes: TxElement.FullOutputBytes,
    val outputAmount: TxElement.OutputAmount,
    val scriptPubKey: TxElement.ScriptPubKey,
)

sealed class TxElement {
    class FullTx(val bytes: ByteArray): TxElement()
    class Version(val bytes: ByteArray): TxElement()
    class FullInputBytes(val bytes: ByteArray): TxElement()
    class OutpointTxid(val bytes: ByteArray): TxElement()
    class OutpointVout(val bytes: ByteArray): TxElement()
    class ScriptSig(val bytes: ByteArray): TxElement()
    class Sequence(val bytes: ByteArray): TxElement()
    class FullOutputBytes(val bytes: ByteArray): TxElement()
    class OutputAmount(val bytes: ByteArray): TxElement()
    class ScriptPubKey(val bytes: ByteArray): TxElement()
    class Locktime(val bytes: ByteArray): TxElement()
}

class InputBookend(startIndex: Int, rawTx: ByteArray) {
    // the input is made up of:
    //     32 bytes outpoint txid
    //     4 bytes outpoint vout
    //     1 to 9 bytes varint declaring size of scriptSig
    //     variable number of bytes for scriptSig
    //     4 bytes sequence
    private val scriptSigVarint = VarInt(rawTx[startIndex + 36], rawTx.copyOfRange(startIndex + 36, startIndex + 36 + 9))
    val start = startIndex
    val end = startIndex + 32 + 4 + scriptSigVarint.length + scriptSigVarint.value + 4
}

class OutputBookend(startIndex: Int, rawTx: ByteArray) {
    // the output is made up of:
    //     8 bytes amount
    //     1 to 9 bytes varint declaring size of scriptPubKey
    //     variable number of bytes for scriptSig
    private val scriptPubKeyVarint = VarInt(rawTx[startIndex + 8], rawTx.copyOfRange(startIndex + 8, startIndex + 8 + 9))
    val start = startIndex
    val end = startIndex + 8 + scriptPubKeyVarint.length + scriptPubKeyVarint.value
}

// give this function a starting point and it will parse all your inputs
fun parseInputs(startIndex: Int, rawTx: ByteArray): List<InputData> {
    val inputList: MutableList<InputData> = mutableListOf()
    val inputsVarint = VarInt(rawTx[startIndex], rawTx.copyOfRange(startIndex, startIndex + 9))
    val numInputs = inputsVarint.value
    val bookends = createInputBookends(startIndex + inputsVarint.length, rawTx, numInputs)

    bookends.forEach {
        val fullInputBytes = rawTx.copyOfRange(it.start, it.end)
        val outpointTxid = rawTx.copyOfRange(it.start, it.start + 32)
        val outpointVout = rawTx.copyOfRange(it.start + 32, it.start + 32 + 4)
        val lengthOfScriptSigVarint = VarInt(rawTx[it.start + 32 + 4], rawTx.copyOfRange(it.start + 32 + 4, it.start + 32 + 4 + 9))
        val scriptSig = rawTx.copyOfRange(
            it.start + 32 + 4 + lengthOfScriptSigVarint.length,
            it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value
        )
        val sequence = rawTx.copyOfRange(
            it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value,
            it.start + 32 + 4 + lengthOfScriptSigVarint.length + lengthOfScriptSigVarint.value + 4
        )

        inputList.add(
            InputData(
                TxElement.FullInputBytes(fullInputBytes),
                TxElement.OutpointTxid(outpointTxid),
                TxElement.OutpointVout(outpointVout),
                TxElement.ScriptSig(scriptSig),
                TxElement.Sequence(sequence)
            )
        )
    }

    return inputList
}

fun createInputBookends(startIndex: Int, rawTx: ByteArray, numInputs: Int): List<InputBookend> {
    val bookendList: MutableList<InputBookend> = mutableListOf()
    for (i: Int in 0 until numInputs) {
        if (i == 0) {
            val bookend = InputBookend(startIndex, rawTx)
            bookendList.add(bookend)
        } else {
            val bookend = InputBookend(bookendList[i - 1].end, rawTx)
            bookendList.add(bookend)
        }
    }

    return bookendList
}

// give this function a starting point and it will parse all your outputs
fun parseOutputs(startIndex: Int, rawTx: ByteArray): List<OutputData> {
    val outputList: MutableList<OutputData> = mutableListOf()
    val outputVarint = VarInt(rawTx[startIndex], rawTx.copyOfRange(startIndex, startIndex + 9))
    val numOutputs = outputVarint.value
    val bookends = createOutputBookends(startIndex + outputVarint.length, rawTx, numOutputs)

    bookends.forEach {
        val fullOutputBytes = rawTx.copyOfRange(it.start, it.end)
        val outputAmount = rawTx.copyOfRange(it.start, it.start + 8)
        val lengthOfScriptPubKeyVarint = VarInt(rawTx[it.start + 8], rawTx.copyOfRange(it.start + 8, it.start + 8 + 9))
        val scriptPubKey = rawTx.copyOfRange(
            it.start + 8,
            it.start + 8 + lengthOfScriptPubKeyVarint.length + lengthOfScriptPubKeyVarint.value
        )

        outputList.add(
            OutputData(
                TxElement.FullOutputBytes(fullOutputBytes),
                TxElement.OutputAmount(outputAmount),
                TxElement.ScriptPubKey(scriptPubKey)
            )
        )
    }

    return outputList
}

fun createOutputBookends(startIndex: Int, rawTx: ByteArray, numOutputs: Int): List<OutputBookend> {
    val bookendList: MutableList<OutputBookend> = mutableListOf()
    for (i: Int in 0 until numOutputs) {
        if (i == 0) {
            val bookend = OutputBookend(startIndex, rawTx)
            bookendList.add(bookend)
        } else {
            val bookend = OutputBookend(bookendList[i - 1].end, rawTx)
            bookendList.add(bookend)
        }
    }

    return bookendList
}


fun calculateInputsSize(inputs: List<InputData>): Int {
    var totalSize: Int = 0
    inputs.forEach {
        totalSize += it.fullInputBytes.bytes.size
    }
    return totalSize
}

fun calculateOutputsSize(outputs: List<OutputData>): Int {
    var totalSize: Int = 0
    outputs.forEach {
        totalSize += it.fullOutputBytes.bytes.size
    }
    return totalSize
}
