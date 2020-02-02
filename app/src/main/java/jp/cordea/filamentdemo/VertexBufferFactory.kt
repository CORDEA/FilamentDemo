package jp.cordea.filamentdemo

import com.google.android.filament.Engine
import com.google.android.filament.VertexBuffer

object VertexBufferFactory {
    fun create(engine: Engine, header: Header, data: BufferData): VertexBuffer {
        val enabled = header.flags and 0x2 != 0
        val uvType = if (enabled) {
            VertexBuffer.AttributeType.SHORT2
        } else {
            VertexBuffer.AttributeType.HALF2
        }
        return VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(header.totalVertices)
            .normalized(VertexBuffer.VertexAttribute.COLOR)
            .normalized(VertexBuffer.VertexAttribute.TANGENTS)
            .attribute(
                VertexBuffer.VertexAttribute.POSITION,
                0,
                VertexBuffer.AttributeType.HALF4,
                header.posOffset,
                header.positionStride
            )
            .attribute(
                VertexBuffer.VertexAttribute.TANGENTS,
                0,
                VertexBuffer.AttributeType.SHORT4,
                header.tangentOffset,
                header.tangentStride
            )
            .attribute(
                VertexBuffer.VertexAttribute.COLOR,
                0,
                VertexBuffer.AttributeType.UBYTE4,
                header.colorOffset,
                header.colorStride
            )
            .attribute(
                VertexBuffer.VertexAttribute.UV0,
                0,
                uvType,
                header.uv0Offset,
                header.uv0Stride
            )
            .normalized(VertexBuffer.VertexAttribute.UV0, enabled)
            .apply {
                if (header.uv1Offset >= 0 && header.uv1Stride >= 0) {
                    attribute(
                        VertexBuffer.VertexAttribute.UV1,
                        0,
                        uvType,
                        header.uv1Offset,
                        header.uv1Stride
                    )
                    normalized(VertexBuffer.VertexAttribute.UV1, enabled)
                }
            }
            .build(engine)
            .apply { setBufferAt(engine, 0, data.value) }
    }
}
