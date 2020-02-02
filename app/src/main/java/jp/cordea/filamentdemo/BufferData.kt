package jp.cordea.filamentdemo

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ReadableByteChannel

inline class BufferData(val value: ByteBuffer) {
    companion object {
        fun from(channel: ReadableByteChannel, sizeInBytes: Int) =
            BufferData(ByteBuffer.allocateDirect(sizeInBytes).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                channel.read(this)
                flip()
            })
    }
}
