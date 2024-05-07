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
    }

    @Nested
    inner class Failure {
        @Test
        fun `Test 1`() {
        }
    }
}