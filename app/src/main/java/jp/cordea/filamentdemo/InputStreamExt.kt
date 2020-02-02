package jp.cordea.filamentdemo

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun InputStream.readFloat32LE() =
    ByteBuffer
        .wrap(ByteArray(4).apply { read(this, 0, 4) })
        .order(ByteOrder.LITTLE_ENDIAN)
        .float

fun InputStream.readUIntLE(): Int =
    ((read() and 0xFF or
            (read() and 0xFF shl 8) or
            (read() and 0xFF shl 16) or
            (read() and 0xFF shl 24)
            ).toLong() and 0xFFFFFFFFL).toInt()
