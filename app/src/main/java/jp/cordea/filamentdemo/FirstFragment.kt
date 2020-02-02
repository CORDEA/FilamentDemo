package jp.cordea.filamentdemo

import android.animation.ValueAnimator
import android.opengl.Matrix
import android.os.Bundle
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
            setParameter("baseColor", Colors.RgbType.SRGB, 0f, 0.7f, 0f)
        }
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
            .intensity(110000f)
            .direction(0f, -0.5f, -1f)
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
