package me.tb.txparser.txelements

data class Output(
    val outputAmount: OutputAmount,
    val scriptPubKey: ScriptPubKey,
)
