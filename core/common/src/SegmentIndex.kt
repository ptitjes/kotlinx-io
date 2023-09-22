package kotlinx.io

internal class SegmentIndex private constructor(
    private val indexes: List<SegmentIndexElement>,
) {
    operator fun get(index: Long): SegmentIndexElement {
        val segmentIndex = indexes.binarySearch { it.compareToIndex(index) }
        if (segmentIndex == -1) throw IllegalArgumentException()
        return indexes[segmentIndex]
    }

    companion object {
        fun build(buffer: Buffer): SegmentIndex {
            val head = buffer.head ?: return SegmentIndex(listOf())

            val indexes = buildList {
                var offset = 0L
                var curr = head
                do {
                    add(SegmentIndexElement(offset, curr))
                    offset += curr.size
                    curr = curr.next!!
                } while (curr != head)
            }

            return SegmentIndex(indexes)
        }
    }
}

internal data class SegmentIndexElement(
    val offset: Long,
    val segment: Segment,
) {
    fun compareToIndex(index: Long): Int {
        val end = offset + segment.size

        if (index < offset) return 1
        if (index >= end) return -1
        return 0
    }
}
