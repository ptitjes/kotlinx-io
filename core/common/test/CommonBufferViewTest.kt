package kotlinx.io

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonBufferViewTest {

    @Test
    fun getShort() {
        val buffer = Buffer()
        val shortCount = Segment.SIZE * 4
        repeat(shortCount) {
            buffer.writeShort(it.toShort())
        }

        val byteCount = (shortCount * 2).toLong()
        val view = buffer.readView(byteCount)

        assertEquals(0.toShort(), view.getShort(0))
        assertEquals(0.toShort(), view.getShort(0)) // getShort doesn't mutate!
        assertEquals((shortCount / 2 - 1).toShort(), view.getShort(byteCount / 2 - 2))
        assertEquals((shortCount - 1).toShort(), view.getShort(byteCount - 2))
    }
}
