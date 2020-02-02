package jp.cordea.filamentdemo

import com.google.android.filament.Box
import java.io.InputStream

class Header(
    val versionNumber: Long,
    val parts: Long,
    val aabb: Box,
    val flags: Long,
    val posOffset: Long,
    val positionStride: Long,
    val tangentOffset: Long,
    val tangentStride: Long,
    val colorOffset: Long,
    val colorStride: Long,
    val uv0Offset: Long,
    val uv0Stride: Long,
    val uv1Offset: Long,
    val uv1Stride: Long,
    val totalVertices: Long,
    val verticesSizeInBytes: Long,
    val indices16Bit: Long,
    val totalIndices: Long,
    val indicesSizeInBytes: Long
) {
    companion object {
        fun from(stream: InputStream) =
            Header(
                stream.readUIntLE(),
                stream.readUIntLE(),
                Box(
                    stream.readFloat32LE(),
                    stream.readFloat32LE(),
                    stream.readFloat32LE(),
                    stream.readFloat32LE(),
                    stream.readFloat32LE(),
                    stream.readFloat32LE()
                ),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE(),
                stream.readUIntLE()
            )
    }
}
