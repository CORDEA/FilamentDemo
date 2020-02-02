package jp.cordea.filamentdemo

import android.content.res.AssetManager
import com.google.android.filament.*
import java.nio.channels.Channels

class Mesh(
    val renderable: Renderable,
    val indexBuffer: IndexBuffer,
    val vertexBuffer: VertexBuffer,
    val aabb: Box
) {
    companion object {
        fun from(
            assets: AssetManager,
            name: String,
            materials: Map<MaterialName, MaterialInstance>,
            engine: Engine
        ) = assets.open(name).use { stream ->
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
    }
}
