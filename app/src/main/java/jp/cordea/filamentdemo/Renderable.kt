package jp.cordea.filamentdemo

import com.google.android.filament.*

class Renderable(@Entity val entity: Int) {
    companion object {
        fun from(
            engine: Engine,
            header: Header,
            indexBuffer: IndexBuffer,
            vertexBuffer: VertexBuffer,
            parts: List<Part>,
            materialNames: List<MaterialName>,
            materials: Map<MaterialName, MaterialInstance>
        ): Renderable {
            val manager = EntityManager.get().create()
            RenderableManager
                .Builder(header.parts)
                .boundingBox(header.aabb)
                .apply {
                    repeat(header.parts) {
                        val part = parts[it]
                        geometry(
                            it,
                            RenderableManager.PrimitiveType.TRIANGLES,
                            vertexBuffer,
                            indexBuffer,
                            part.offset,
                            part.minIndex,
                            part.maxIndex,
                            part.indexCount
                        )

                        material(it, materials.getValue(materialNames[part.materialId]))
                    }
                }
                .build(engine, manager)
            return Renderable(manager)
        }
    }
}
