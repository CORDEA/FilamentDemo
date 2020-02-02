package jp.cordea.filamentdemo

import com.google.android.filament.Engine
import com.google.android.filament.IndexBuffer

object IndexBufferFactory {
    fun create(engine: Engine, header: Header, data: BufferData) =
        IndexBuffer.Builder()
            .bufferType(
                if (header.indices16Bit == 0) {
                    IndexBuffer.Builder.IndexType.UINT
                } else {
                    IndexBuffer.Builder.IndexType.USHORT
                }
            )
            .indexCount(header.totalIndices)
            .build(engine)
            .apply { setBuffer(engine, data.value) }
}
