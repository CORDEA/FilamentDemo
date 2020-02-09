package jp.cordea.filamentdemo

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.util.SizeF
import android.view.Surface
import com.google.android.filament.*

class BackNormalTexture(
    private val context: Context,
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
        materialInstance.setParameter("backNormalTexture", texture, sampler)
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

    fun draw() {
        val canvas = canvasSurface.lockCanvas(null)
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.back_normal)
        canvas.drawBitmap(bitmap, null, RectF(0f, 0f, size.width, size.height), null)
        bitmap.recycle()
        canvasSurface.unlockCanvasAndPost(canvas)
    }
}
