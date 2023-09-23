package kotlinx.io

public fun Source.readView(byteCount: Long): BufferView {
    val buffer = Buffer()
    buffer.write(this, byteCount)
    return BufferView(buffer)
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
            val s = getByte(position) and 0xff shl 8 or (getByte(position + 1) and 0xff)
            return s.toShort()
        }

        val s = segment[segmentPosition] and 0xff shl 8 or (segment[segmentPosition + 1] and 0xff)
        return s.toShort()
    }

    public fun getInt(position: Long): Int {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position + 4)

        val element = segmentIndex!![position]
        val segment = element.segment

        val segmentPosition = (position - element.offset).toInt()
        if (segment.size - segmentPosition < 4) {
            return (
                    getByte(position) and 0xff shl 24
                            or (getByte(position + 1) and 0xff shl 16)
                            or (getByte(position + 2) and 0xff shl 8)
                            or (getByte(position + 3) and 0xff)
                    )
        }

        return (
                segment[segmentPosition] and 0xff shl 24
                        or (segment[segmentPosition + 1] and 0xff shl 16)
                        or (segment[segmentPosition + 2] and 0xff shl 8)
                        or (segment[segmentPosition + 3] and 0xff)
                )
    }

    public fun getLong(position: Long): Long {
        if (segmentIndex == null) throw EOFException("BufferView has been closed")
        checkBounds(size, position, position + 8)

        val element = segmentIndex!![position]
        val segment = element.segment

        val segmentPosition = (position - element.offset).toInt()
        if (segment.size - segmentPosition < 8) {
            return (
                    getInt(position) and 0xffffffffL shl 32
                            or (getInt(position + 4) and 0xffffffffL)
                    )
        }

        return (
                segment[segmentPosition] and 0xffL shl 56
                        or (segment[segmentPosition + 1] and 0xffL shl 48)
                        or (segment[segmentPosition + 2] and 0xffL shl 40)
                        or (segment[segmentPosition + 3] and 0xffL shl 32)
                        or (segment[segmentPosition + 4] and 0xffL shl 24)
                        or (segment[segmentPosition + 5] and 0xffL shl 16)
                        or (segment[segmentPosition + 6] and 0xffL shl 8)
                        or (segment[segmentPosition + 7] and 0xffL)
                )
    }

    public fun slice(
        startIndex: Long = 0L,
        endIndex: Long = size
    ): BufferView {
        if (buffer == null) throw EOFException("BufferView has been closed")
        checkBounds(size, startIndex, endIndex)

        val copy = Buffer()
        buffer!!.copyTo(copy, startIndex, endIndex)
        return BufferView(copy)
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
