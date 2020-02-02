package jp.cordea.filamentdemo

import com.google.android.filament.Box
import java.io.InputStream

class Part(
    val offset: Int,
    val indexCount: Int,
    val minIndex: Int,
    val maxIndex: Int,
    val materialId: Int,
    val aabb: Box
) {
    companion object {
        fun from(header: Header, stream: InputStream) =
            (0 until header.parts).map {
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
