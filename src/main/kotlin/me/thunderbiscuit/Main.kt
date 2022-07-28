package me.thunderbiscuit

fun main() {

    // val tx0: Tx = Tx(hexTx4)
    val txDataStructure: TxDataStructure = txParser(hexTx4.toByteArray())
    println(txDataStructure.inputs[0].outPointVout.bytes.toHex())

    val tx4Parent: TxDataStructure = txParser(hexTx4Parent.toByteArray())
    println(tx4Parent.getScriptPubKey(1).toHex())




    // parse tx
    // println("Full tx size is ${tx0.txSize} bytes")
    // println("Txid: ${tx0.txid}")
    // println("Version number: ${tx0.version}")
    // println("Number of inputs: ${tx0.numInputs}")
    // println("Inputs: ${tx0.inputs}")
    // println("Number of outputs: ${tx0.numOutputs}")
    // println("Outputs: ${tx0.outputs}")
    // println(tx0.locktime)
    // println("Sighash: ${sigHashTo4Bytes(SigHash.SIGHASH_ALL)}, ${sigHashTo4Bytes(SigHash.SIGHASH_ALL).toHex()}")

    // validate tx
    // println(tx0.validateTransaction())
}
