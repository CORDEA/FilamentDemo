package jp.cordea.filamentdemo

import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.opengl.Matrix
import android.util.SizeF
import android.view.Surface
import com.google.android.filament.*

class FrontTexture(
    val engine: Engine,
    val materialInstance: MaterialInstance,
    val size: SizeF,
    val rotation: Int
) {
    private val surfaceTexture: SurfaceTexture
    private val canvasSurface: Surface
    private val stream: Stream

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val texture: Texture =
        Texture.Builder()
            .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
            .format(Texture.InternalFormat.RGB8)
            .build(engine)

    init {
        val transform = FloatArray(16)
        val ratio = size.width / size.height
        when (rotation) {
            Surface.ROTATION_0 -> {
                Matrix.translateM(transform, 0, 1f, 0f, 0f)
                Matrix.rotateM(transform, 0, 90f, 0f, 0f, 1f)
                Matrix.translateM(transform, 0, 1f, 0f, 0f)
                Matrix.scaleM(transform, 0, -1f, 1f / ratio, 1f)
            }
            Surface.ROTATION_90 -> {
                Matrix.translateM(transform, 0, 1f, 1f, 0f)
                Matrix.rotateM(transform, 0, 180f, 0f, 0f, 1f)
                Matrix.translateM(transform, 0, 1f, 0f, 0f)
                Matrix.scaleM(transform, 0, -1f / ratio, 1f, 1f)
            }
            Surface.ROTATION_270 -> {
                Matrix.translateM(transform, 0, 1f, 0f, 0f)
                Matrix.scaleM(transform, 0, -1f / ratio, 1f, 1f)
            }
        }

        val sampler = TextureSampler(
            TextureSampler.MinFilter.LINEAR,
            TextureSampler.MagFilter.LINEAR,
            TextureSampler.WrapMode.REPEAT
        )

        materialInstance.setParameter("frontTexture", texture, sampler)
        materialInstance.setParameter(
            "textureTransform",
            MaterialInstance.FloatElement.MAT4,
            transform,
            0,
            1
        )

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
        canvas.drawRect(0f, 0f, size.width, size.height, paint)
        canvasSurface.unlockCanvasAndPost(canvas)
    }
}
