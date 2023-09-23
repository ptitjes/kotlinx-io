package kotlinx.io.internal

import kotlinx.io.and

internal fun asShort(
    byte1: Byte,
    byte2: Byte,
): Short {
    return (
            byte1 and 0xff shl 8
                    or (byte2 and 0xff)
            ).toShort()
}

internal fun asInt(
    byte1: Byte,
    byte2: Byte,
    byte3: Byte,
    byte4: Byte,
): Int {
    return (
            byte1 and 0xff shl 24
                    or (byte2 and 0xff shl 16)
                    or (byte3 and 0xff shl 8)
                    or (byte4 and 0xff)
            )

}

internal fun asLong(
    int1: Int,
    int2: Int,
): Long {
    return int1 and 0xffffffffL shl 32 or (int2 and 0xffffffffL)
}

internal fun asLong(
    byte1: Byte,
    byte2: Byte,
    byte3: Byte,
    byte4: Byte,
    byte5: Byte,
    byte6: Byte,
    byte7: Byte,
    byte8: Byte,
): Long {
    return (
            byte1 and 0xffL shl 56
                    or (byte2 and 0xffL shl 48)
                    or (byte3 and 0xffL shl 40)
                    or (byte4 and 0xffL shl 32)
                    or (byte5 and 0xffL shl 24)
                    or (byte6 and 0xffL shl 16)
                    or (byte7 and 0xffL shl 8)
                    or (byte8 and 0xffL)
            )
}
