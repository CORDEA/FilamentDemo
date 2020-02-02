package jp.cordea.filamentdemo

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

    @Entity
    private var light = 0

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
                override fun onNativeWindowChanged(surface: Surface?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResized(width: Int, height: Int) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDetachedFromSurface() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        filamentView.setClearColor(0.035f, 0.035f, 0.035f, 1f)
        filamentView.camera = camera
        filamentView.scene = scene
    }

    private fun setupScene() {
        material = requireContext().assets.openFd("...").use { fd ->
            val stream = fd.createInputStream()
            val dst = ByteBuffer.allocate(fd.length.toInt())
            Channels.newChannel(stream).use { it.read(dst) }

            val buffer = dst.apply { rewind() }
            Material.Builder().payload(buffer, buffer.remaining()).build(engine)
        }
        materialInstance = material.createInstance()

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
            0.0,
            0.0,
            5.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0
        )
    }

    override fun doFrame(frameTimeNanos: Long) {
        choreographer.postFrameCallback(this)
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
        engine.destroyRenderer(renderer)
        engine.destroyView(filamentView)
        engine.destroyScene(scene)
        engine.destroyCamera(camera)

        engine.destroy()
    }
}
