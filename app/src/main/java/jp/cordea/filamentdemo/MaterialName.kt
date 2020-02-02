package jp.cordea.filamentdemo

import java.io.InputStream
import java.nio.charset.StandardCharsets

inline class MaterialName(val value: String) {
    companion object {
        fun from(stream: InputStream) =
            (0 until stream.readUIntLE()).map {
                val data = ByteArray(stream.readUIntLE())
                stream.read(data)
                stream.skip(1)
                data.toString(StandardCharsets.UTF_8)
            }.map { MaterialName(it) }
    }
}
