package me.tb.txparser.txelements

import me.tb.txparser.Witness

typealias ScriptSigVarInt = VarInt

data class Input(
    val outPointTxid: OutpointTxid,
    val outPointVout: OutpointVout,
    val scriptSigVarInt: ScriptSigVarInt,
    val scriptSig: ScriptSig,
    val sequence: Sequence,
    var witness: Witness? = null
)
