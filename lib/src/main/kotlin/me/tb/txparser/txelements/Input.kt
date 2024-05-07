package me.tb.txparser.txelements

typealias ScriptSigVarInt = VarInt

data class Input(
    val outPointTxid: OutpointTxid,
    val outPointVout: OutpointVout,
    val scriptSigVarInt: ScriptSigVarInt,
    val scriptSig: ScriptSig,
    val sequence: Sequence,
)
