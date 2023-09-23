package kotlinx.io

import kotlinx.io.internal.asInt
import kotlinx.io.internal.asLong
import kotlinx.io.internal.asShort

public fun Source.readView(byteCount: Long): BufferView {
    val buffer = Buffer()
    buffer.write(this, byteCount)
    return BufferView(buffer)
}

public fun Buffer.copyToView(
    startIndex: Long = 0L,
    endIndex: Long = size,
): BufferView {
    val copy = Buffer()
    this.copyTo(copy, startIndex, endIndex)
    return BufferView(copy)
}

/**
 * Not multithread-safe.
 */
@OptIn(ExperimentalStdlibApi::class)
public class BufferView internal constructor(buffer: Buffer) : AutoCloseableAlias {

    private var buffer: Buffer? = buffer
    private var segmentIndex: SegmentIndex? = SegmentIndex.build(buffer)

    override fun close() {
        buffer?.clear()
        buffer = null
        segmentIndex = null
    }

    public val size: Long
        get() {
            if (buffer == null) throw EOFException("BufferView has been closed")
            return buffer!!.size
        }

    public fun getByte(position: Long): Byte {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position)

        val element = segmentIndex!![position]
        return element.segment[(position - element.offset).toInt()]
    }

    public fun getShort(position: Long): Short {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position + 2)

        val element = segmentIndex!![position]
        val segment = element.segment

        val segmentPosition = (position - element.offset).toInt()
        if (segment.size - segmentPosition < 2) {
            return asShort(getByte(position), getByte(position + 1))
        }

        return asShort(segment[segmentPosition], segment[segmentPosition + 1])
    }

    public fun getInt(position: Long): Int {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position + 4)

        val element = segmentIndex!![position]
        val segment = element.segment

        val segmentPosition = (position - element.offset).toInt()
        if (segment.size - segmentPosition < 4) {
            return asInt(
                getByte(position),
                getByte(position + 1),
                getByte(position + 2),
                getByte(position + 3)
            )
        }

        return asInt(
            segment[segmentPosition],
            segment[segmentPosition + 1],
            segment[segmentPosition + 2],
            segment[segmentPosition + 3]
        )
    }

    public fun getLong(position: Long): Long {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position + 8)

        val element = segmentIndex!![position]
        val segment = element.segment

        val segmentPosition = (position - element.offset).toInt()
        if (segment.size - segmentPosition < 8) {
            return asLong(getInt(position), getInt(position + 4))
        }

        return asLong(
            segment[segmentPosition],
            segment[segmentPosition + 1],
            segment[segmentPosition + 2],
            segment[segmentPosition + 3],
            segment[segmentPosition + 4],
            segment[segmentPosition + 5],
            segment[segmentPosition + 6],
            segment[segmentPosition + 7]
        )
    }

    public fun slice(
        startIndex: Long = 0L,
        endIndex: Long = size
    ): BufferView {
        if (buffer == null) throw EOFException("BufferView has been closed")
        checkBounds(size, startIndex, endIndex)

        return buffer!!.copyToView(startIndex, endIndex)
    }
}

public fun BufferView.getShortLe(position: Long): Short = getShort(position).reverseBytes()
public fun BufferView.getIntLe(position: Long): Int = getInt(position).reverseBytes()
public fun BufferView.getLongLe(position: Long): Long = getLong(position).reverseBytes()

public fun BufferView.getUShortLe(position: Long): UShort = getShortLe(position).toUShort()
public fun BufferView.getUIntLe(position: Long): UInt = getIntLe(position).toUInt()
public fun BufferView.getULongLe(position: Long): ULong = getLongLe(position).toULong()

public fun BufferView.getFloat(position: Long): Float = Float.fromBits(getInt(position))
public fun BufferView.getDouble(position: Long): Double = Double.fromBits(getLong(position))

public fun BufferView.getFloatLe(position: Long): Float = Float.fromBits(getIntLe(position))
public fun BufferView.getDoubleLe(position: Long): Double = Double.fromBits(getLongLe(position))

// TODO Replace with proper segment content access (cf. https://github.com/Kotlin/kotlinx-io/issues/135)
private operator fun Segment.get(index: Int): Byte {
    require(index in 0..<size)
    return data[pos + index]
}
