package aetherflow.game

import org.eclipse.fx.drift.*
import javafx.application.*
import javafx.geometry.*
import javafx.stage.*
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.*
import org.lwjgl.opengl.*
import org.lwjgl.system.*
import org.lwjgl.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.system.MemoryUtil.*

object MainApp {
  def main(args: Array[String]): Unit = {
    Configuration.DEBUG.set(true)
    Configuration.DEBUG_FUNCTIONS.set(false)
    Configuration.DEBUG_STREAM.set(System.err)

    Application.launch(classOf[MyApp], args: _*)
  }
}

class MyApp extends Application {
  var width = 1024
  var height = 768
  var fov: Float = 60

  var alive: Boolean = false
  var txType: TransferType = StandardTransferTypes.MainMemory
  var rotation: Float = 0

  var debugProc: Callback = null
  var renderer: Renderer = null
  var swapChain: Swapchain = null

  // Starts the JavaFX window
  override def start(stage: Stage): Unit = {
    val root = new BorderPane()
    root.setBackground(null)
    root.setPadding(new Insets(40))
    root.setPrefSize(width, height)

    val scene = new Scene(root)
    scene.setFill(Color.TRANSPARENT)

    val surface = new DriftFXSurface

    renderer = GLRenderer.getRenderer(surface)

    root.setCenter(surface)

    val start = new Button("Start")
    start.setOnAction(e => {
       val t = new Thread(() => run);
       t.setDaemon(true);
       t.start();
    })
    val stop = new Button("Stop")
    stop.setOnAction(_ => {
      alive = false
    })

    val txMode = new ComboBox[TransferType]()
    txMode.getItems.addAll(
      StandardTransferTypes.MainMemory,
      StandardTransferTypes.NVDXInterop,
      StandardTransferTypes.IOSurface
    )
    txMode.setValue(txType)
    txMode.valueProperty().addListener((obs, ov, nv) => txType = nv)

    val buttons = new HBox(start, stop, txMode)
    root.setTop(buttons)

    surface.setOnMouseMoved(event => {
      rotation = (event.getX.asInstanceOf[Float] / width - 0.5f) * 2f * Math.PI.asInstanceOf[Float]
    })
    surface.setOnScroll(event => {
      if (event.getDeltaY < 0) fov *= 1.05f
      else fov *= 1f / 1.05f
      if (fov < 10.0f) fov = 10.0f
      else if (fov > 120.0f) fov = 120.0f
    })

    stage.setTitle("LWJGL in JavaFX in Scala")
    stage.setScene(scene)
    stage.setOnCloseRequest(event => {
      alive = false
    })
    stage.show()
  }

  // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // TODO !!! 1. Run examples from these sites, make sure they work locally https://github.com/BestSolution-at/efxclipse-drift-samples
  // TODO !!! 2. Transfer the example fully to this Test.scala and make sure it works identically.
  // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

  // Starts the lwjgl window embedded
  def run: Unit = {
    alive = true
    try {
      doInit
      loop()
      if (debugProc != null) {
        debugProc.free()
      }
    } catch {
      case t: Throwable => t.printStackTrace()
    }
  }

  var ctx: Long = 0
  var curTxType: TransferType = StandardTransferTypes.MainMemory

  def doInit: Unit = {
    ctx = org.eclipse.fx.drift.internal.GL.createSharedCompatContext(0)
    org.eclipse.fx.drift.internal.GL.makeContextCurrent(ctx)
    System.err.println("Context is " + ctx)

    val caps = GL.createCapabilities()
    System.err.println("CAPS: " + caps.OpenGL32)
    System.err.println("ARB_shader_objects = " + caps.GL_ARB_shader_objects
      + ", ARB_vertex_shader = " + caps.GL_ARB_vertex_shader
      + ", ARB_fragment_shader = " + caps.GL_ARB_fragment_shader)

    debugProc = GLUtil.setupDebugMessageCallback()

    glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
    glEnable(GL_DEPTH_TEST)
  }

  def loop(): Unit = {
    while (alive) {
      val size = renderer.getSize
      if (swapChain == null  || (curTxType ne txType)) {
        System.err.println("(re)create swapchain")
        if (swapChain != null) swapChain.dispose()
        swapChain = renderer.createSwapchain(new SwapchainConfig(size, 2, PresentationMode.MAILBOX, txType))
        curTxType = txType
      }
      try {
        System.err.println(s"Size: ${size.x}, ${size.y}")
        System.err.println(s"SwapChain: $swapChain")
        val target = swapChain.acquire
        glClear(GL_COLOR_BUFFER_BIT)
        swapChain.present(target)
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    }

    swapChain.dispose()
    swapChain = null
  }
}
