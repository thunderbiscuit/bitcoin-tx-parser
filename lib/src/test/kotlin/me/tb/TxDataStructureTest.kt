package me.tb

import me.tb.txparser.TxDataStructure
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TxDataStructureTest {
    @Nested
    inner class Success {
        @Test
        fun `TxDataStructure correctly parses inputs`() {
            val tx: TxDataStructure = TxDataStructure.fromRawTx(hexTx3.hexToUByteArray())
            println(tx.inputs)
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `Segwit transactions are not supported`() {
            assertFailsWith<IllegalArgumentException> {
                TxDataStructure.fromRawTx(hexTx4.hexToUByteArray())
            }
        }
    }
}
