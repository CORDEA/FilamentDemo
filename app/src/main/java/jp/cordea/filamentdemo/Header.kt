package jp.cordea.filamentdemo

import com.google.android.filament.Box
import java.io.InputStream

class Header(
    val versionNumber: Int,
    val parts: Int,
    val aabb: Box,
    val flags: Int,
    val positionOffset: Int,
    val positionStride: Int,
    val tangentOffset: Int,
    val tangentStride: Int,
    val colorOffset: Int,
    val colorStride: Int,
    val uv0Offset: Int,
    val uv0Stride: Int,
    val uv1Offset: Int,
    val uv1Stride: Int,
    val totalVertices: Int,
    val verticesSizeInBytes: Int,
    val indices16Bit: Int,
    val totalIndices: Int,
    val indicesSizeInBytes: Int
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
