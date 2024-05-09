package me.tb

import me.tb.txparser.Tx
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class TxTest {
    @Nested
    inner class Success {
        @Test
        fun `TxDataStructure correctly parses inputs`() {
            val tx: Tx = Tx.fromRawTx(hexTx3.hexToUByteArray())
            println(tx.inputs)
        }

        @Test
        fun `Segwit transactions are supported`() {
            val tx = Tx.fromRawTx(hexTx5.hexToUByteArray())
            println(tx.inputs.forEach(::println))
        }
    }
}
