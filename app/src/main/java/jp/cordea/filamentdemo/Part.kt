package jp.cordea.filamentdemo

import com.google.android.filament.Box
import java.io.InputStream

class Part(
    val offset: Long,
    val indexCount: Long,
    val minIndex: Long,
    val maxIndex: Long,
    val materialId: Long,
    val aabb: Box
) {
    companion object {
        fun from(header: Header, stream: InputStream) =
            (0 until header.parts.toInt()).map {
                Part(
                    stream.readUIntLE(),
                    stream.readUIntLE(),
                    stream.readUIntLE(),
                    stream.readUIntLE(),
                    stream.readUIntLE(),
                    Box(
                        stream.readFloat32LE(),
                        stream.readFloat32LE(),
                        stream.readFloat32LE(),
                        stream.readFloat32LE(),
                        stream.readFloat32LE(),
                        stream.readFloat32LE()
                    )
                )
            }
    }
}
