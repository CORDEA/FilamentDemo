package jp.cordea.filamentdemo

import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.util.SizeF
import android.view.Surface
import com.google.android.filament.*

class CanvasTexture(
    name: String,
    engine: Engine,
    materialInstance: MaterialInstance,
    private val size: SizeF
) {
    private val surfaceTexture: SurfaceTexture
    private val canvasSurface: Surface
    private val stream: Stream

    private val texture: Texture =
        Texture.Builder()
            .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
            .format(Texture.InternalFormat.RGB8)
            .build(engine)

    init {
        val sampler = TextureSampler(
            TextureSampler.MinFilter.LINEAR,
            TextureSampler.MagFilter.LINEAR,
            TextureSampler.WrapMode.REPEAT
        )
        materialInstance.setParameter(name, texture, sampler)
        surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(size.width.toInt(), size.height.toInt())
            detachFromGLContext()
        }
        canvasSurface = Surface(surfaceTexture)

        stream = Stream.Builder()
            .stream(surfaceTexture)
            .build(engine)

        texture.setExternalStream(engine, stream)
    }

    fun draw(draw: (canvas: Canvas) -> Unit) {
        val canvas = canvasSurface.lockCanvas(null)
        draw(canvas)
        canvasSurface.unlockCanvasAndPost(canvas)
    }
}
