package me.tb.txelements

import me.tb.txparser.txelements.Version
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VersionTest {
    @Nested
    inner class Success {
        @Test
        fun `Version 1 is correctly parsed`() {
            val version = Version("01000000".hexToUByteArray())
            assertEquals(1u, version.value)
        }

        @Test
        fun `Max version is correctly parsed`() {
            val version = Version("ffffffff".hexToUByteArray())
            assertEquals(
                expected = UInt.MAX_VALUE,
                actual = version.value
            )
        }
    }
    @Nested
    inner class Failure {
        @Test
        fun `Big-endian version 1 doesn't work`() {
            assertFalse {
                val version = Version("00000001".hexToUByteArray())
                version.value == 1u
            }
        }
    }
}
