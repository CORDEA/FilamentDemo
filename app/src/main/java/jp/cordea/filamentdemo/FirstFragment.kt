package jp.cordea.filamentdemo

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.Matrix
import android.os.Bundle
import android.util.SizeF
import android.view.*
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.filament.*
import com.google.android.filament.android.UiHelper
import java.nio.ByteBuffer
import java.nio.channels.Channels

class FirstFragment : Fragment(), Choreographer.FrameCallback {
    companion object {
        init {
            Filament.init()
        }
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var uiHelper: UiHelper
    private lateinit var choreographer: Choreographer
    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var scene: Scene
    private lateinit var filamentView: com.google.android.filament.View
    private lateinit var camera: Camera
    private lateinit var material: Material
    private lateinit var materialInstance: MaterialInstance
    private lateinit var mesh: Mesh

    @Entity
    private var light = 0
    private var swapChain: SwapChain? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_first, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceView = view.findViewById(R.id.surface_view)
        choreographer = Choreographer.getInstance()
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            renderCallback = object : UiHelper.RendererCallback {
                override fun onNativeWindowChanged(surface: Surface) {
                    swapChain?.let { engine.destroySwapChain(it) }
                    swapChain = engine.createSwapChain(surface)
                }

                override fun onResized(width: Int, height: Int) {
                    camera.setProjection(
                        45.0,
                        width.toDouble() / height.toDouble(),
                        0.1,
                        20.0,
                        Camera.Fov.VERTICAL
                    )
                    filamentView.viewport = Viewport(0, 0, width, height)
                }

                override fun onDetachedFromSurface() {
                    swapChain?.let {
                        engine.destroySwapChain(it)
                        engine.flushAndWait()
                        swapChain = null
                    }
                }

            }
            attachTo(surfaceView)
        }

        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        filamentView = engine.createView()
        camera = engine.createCamera()

        setupView()
        setupScene()
    }

    private fun setupView() {
        filamentView.setClearColor(1f, 1f, 1f, 1f)
        filamentView.camera = camera
        filamentView.scene = scene
    }

    private fun setupScene() {
        material = requireContext().assets.openFd("lit.filamat").use { fd ->
            val stream = fd.createInputStream()
            val dst = ByteBuffer.allocate(fd.length.toInt())
            Channels.newChannel(stream).use { it.read(dst) }

            val buffer = dst.apply { rewind() }
            Material.Builder().payload(buffer, buffer.remaining()).build(engine)
        }
        materialInstance = material.createInstance().apply {
            setParameter("baseColor", Colors.RgbType.SRGB, 1f, 1f, 0f)
        }

        val size = SizeF(1008f, 625f)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 25f
        }
        CanvasTexture("frontTexture", engine, materialInstance, size).draw {
            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.front)
            it.drawBitmap(bitmap, null, RectF(0f, 0f, size.width, size.height), null)
            it.drawText("Photo by Lukas Kloeppel from Pexels", 50f, 70f, paint)
            bitmap.recycle()
        }
        CanvasTexture("backTexture", engine, materialInstance, size).draw {
            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.back)
            it.drawBitmap(bitmap, null, RectF(0f, 0f, size.width, size.height), null)
            it.drawText("Photo by Septimiu Lupea from Pexels", 50f, 70f, paint)
            bitmap.recycle()
        }
        CanvasTexture("backNormalTexture", engine, materialInstance, size).draw {
            val bitmap =
                BitmapFactory.decodeResource(requireContext().resources, R.drawable.back_normal)
            it.drawBitmap(bitmap, null, RectF(0f, 0f, size.width, size.height), null)
            bitmap.recycle()
        }
        CanvasTexture("frontNormalTexture", engine, materialInstance, size).draw {
            val bitmap =
                BitmapFactory.decodeResource(requireContext().resources, R.drawable.front_normal)
            it.drawBitmap(bitmap, null, RectF(0f, 0f, size.width, size.height), null)
            bitmap.recycle()
        }

        setTextureTransform(size)
        val map = mapOf(MaterialName("Material") to materialInstance)
        mesh = Mesh.from(requireContext().assets, "sample.filamesh", map, engine)
        engine.transformManager.setTransform(
            engine.transformManager.getInstance(mesh.renderable.entity),
            floatArrayOf(
                0.1f, 0f, 0f, 0f,
                0f, 0.1f, 0f, 0f,
                0f, 0f, 0.1f, 0f,
                0f, 0f, 0f, 1f
            )
        )
        scene.addEntity(mesh.renderable.entity)

        light = EntityManager.get().create()
        val (r, g, b) = Colors.cct(5500f)
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(r, g, b)
            .intensity(100000f)
            .direction(-1f, -0.1f, 0f)
            .castShadows(true)
            .build(engine, light)
        scene.addEntity(light)

        camera.setExposure(16f, 1f / 125f, 100f)
        camera.lookAt(
            3.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0
        )

        startAnimation()
    }

    private fun startAnimation() {
        val matrix = FloatArray(16)
        ValueAnimator.ofFloat(0f, 360f)
            .apply {
                duration = 2000L
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    Matrix.setRotateM(matrix, 0, value, 0f, 1f, 0f)
                    Matrix.scaleM(matrix, 0, 0.1f, 0.1f, 0.1f)
                    engine.transformManager.setTransform(
                        engine.transformManager.getInstance(mesh.renderable.entity),
                        matrix
                    )
                }
            }
            .start()
    }

    private fun setTextureTransform(size: SizeF) {
        val transform = FloatArray(16)
        val ratio = size.width / size.height
        Matrix.setIdentityM(transform, 0)
        Matrix.rotateM(transform, 0, 90f, 0f, 1f, 0f)
        Matrix.rotateM(transform, 0, 180f, 1f, 0f, 0f)
        Matrix.translateM(transform, 0, 0f, 0f, 0.38f)
        Matrix.translateM(transform, 0, 0f, 0.32f, 0f)
        Matrix.scaleM(transform, 0, 1f, ratio * 0.23f, 0.23f)
        materialInstance.setParameter(
            "textureTransform",
            MaterialInstance.FloatElement.MAT4,
            transform,
            0,
            1
        )
    }

    override fun doFrame(frameTimeNanos: Long) {
        choreographer.postFrameCallback(this)
        if (!uiHelper.isReadyToRender) {
            return
        }
        if (!renderer.beginFrame(swapChain!!)) {
            return
        }
        renderer.render(filamentView)
        renderer.endFrame()
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(this)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(this)
        uiHelper.detach()

        engine.destroyEntity(mesh.renderable.entity)
        engine.destroyIndexBuffer(mesh.indexBuffer)
        engine.destroyVertexBuffer(mesh.vertexBuffer)
        EntityManager.get().destroy(mesh.renderable.entity)

        engine.destroyRenderer(renderer)
        engine.destroyMaterialInstance(materialInstance)
        engine.destroyMaterial(material)
        engine.destroyView(filamentView)
        engine.destroyScene(scene)
        engine.destroyCamera(camera)

        EntityManager.get().destroy(light)

        engine.destroy()
    }
}
