package aetherflow.editor

import zio.*
import aetherflow.*
import aetherflow.editor.*
import aetherflow.engine.core.logger.ASyncLogger

abstract class EditorApp(gameApp: engine.App) extends ZIOAppDefault {
  private val logger = new ASyncLogger("Editor-App")

  val program = for {
    _ <- logger.logVerbose("Starting Editor App")
    _ <- performance.MetricClient.run
    performanceMonitorFiber <- performance.Monitor.forkNewWindowApp
    gameFiber <- gameApp.configuredLoggedProgram.fork
    _ <- gameFiber.join
    _ <- logger.logVerbose("Closing Editor App")
    _ <- ZIO.foreachPar(Vector(
      performanceMonitorFiber,
      gameFiber,
    ))(_.interruptFork)
    _ <- logger.logVerbose("Closed Editor App")
  } yield ()

  val configuredProgram = program.provide(
    performance.Monitor.layer(metricSendIntervalMillis = 40), // TODO to conf
    ZLayer.succeed(gameApp.createLogFilter),
  )

  def run = configuredProgram
}
