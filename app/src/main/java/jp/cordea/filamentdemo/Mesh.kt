package jp.cordea.filamentdemo

import android.content.res.AssetManager
import com.google.android.filament.*
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets

class Mesh(
    val renderable: Renderable,
    val indexBuffer: IndexBuffer,
    val vertexBuffer: VertexBuffer,
    val aabb: Box
) {
    companion object {
        private const val IDENTIFIER = "FILAMESH"

        fun from(
            assets: AssetManager,
            name: String,
            materials: Map<MaterialName, MaterialInstance>,
            engine: Engine
        ) = assets.open(name).use { stream ->
            if (!isValid(stream)) {
                throw IllegalArgumentException()
            }
            val header = Header.from(stream)
            val channel = Channels.newChannel(stream)
            val vertexBufferData = BufferData.from(channel, header.verticesSizeInBytes)
            val indexBufferData = BufferData.from(channel, header.indicesSizeInBytes)
            val parts = Part.from(header, stream)
            val materialNames = MaterialName.from(stream)
            val indexBuffer = IndexBufferFactory.create(engine, header, indexBufferData)
            val vertexBuffer = VertexBufferFactory.create(engine, header, vertexBufferData)
            val renderable = Renderable.from(
                engine,
                header,
                indexBuffer,
                vertexBuffer,
                parts,
                materialNames,
                materials
            )
            Mesh(renderable, indexBuffer, vertexBuffer, header.aabb)
        }

        private fun isValid(stream: InputStream) =
            String(
                ByteArray(IDENTIFIER.length).apply { stream.read(this) },
                StandardCharsets.UTF_8
            ) == IDENTIFIER
    }
}
