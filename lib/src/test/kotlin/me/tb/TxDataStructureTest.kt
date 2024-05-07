package me.tb

import me.tb.txparser.TxDataStructure
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class TxDataStructureTest {
    @Nested
    inner class Success {
        @Test
        fun `TxDataStructure correctly parses inputs`() {
            val tx: TxDataStructure = TxDataStructure.fromRawTx(hexTx3.hexToUByteArray())
            println(tx.inputs)
        }

        @Test
        fun `Segwit transactions are supported`() {
            val tx = TxDataStructure.fromRawTx(hexTx5.hexToUByteArray())
            println(tx.inputs.forEach(::println))
        }
    }
}
