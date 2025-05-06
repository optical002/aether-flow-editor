package aetherflow.editor.performance

import aetherflow.engine.core.logger.ASyncLogger
import aetherflow.engine.performance
import zio.ZIO
import zio.metrics.connectors.MetricEvent
import zio.metrics.connectors.internal.MetricsClient
import zio.metrics.{MetricKey, MetricState}

import java.time.Instant

object MetricClient {
  import performance.Data.DataAggregator.*
  
  val logger = new ASyncLogger("Performance.MetricClient")

  val run = for {
    _ <- logger.logVerbose(s"Starting client")
    aggregator <- ZIO.service[DataAggregator]
    startedAt <- ZIO.succeed(Instant.now().toEpochMilli)
    _ <- MetricsClient.make { iter =>
      for {
        collected <- ZIO.collectPar(iter) {
          case MetricEvent.New(metricKey, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Unchanged(metricKey, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Updated(metricKey, _, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
        }
        _ <- logger.logVerbose(
          s"Collected ${collected.flatten.size} metrics. Sending to aggregator: " +
            s"${collected.flatten.map {
              case Entry.Buffer(timestamp, entry) =>
                s"BufferEntry(timestamp = $timestamp, value = ${entry.value}, metricName = ${entry.header.asMetricName})"
              case entry: Entry.Constant  =>
                s"ConstantEntry(value = ${entry.value}, metricName = ${entry.header.asMetricName})"
            }.mkString(start = "[\n  ", sep = "\n  ", end = "\n]")
            }"
        )
        _ <- ZIO.succeed(aggregator.aggregate(collected.flatten))
      } yield ()
    }
  } yield ()


  def processInstance(
   metricKey: MetricKey.Untyped, metricState: MetricState.Untyped, timestamp: Double
  ): Option[Entry] = for {
    path <- fromMetricName(metricKey.name)
    state <- processState(metricState)
  } yield {
    val constantUnit = Entry.UnitConstant(state.doubleValue, path.header)
    val constantNs = Entry.NsConstant(state.doubleValue, path.header)
    path.kind match {
      case Kind.ConstantUnit => constantUnit
      case Kind.ConstantNs => constantNs
      case Kind.BufferUnit   => Entry.Buffer(timestamp, constantUnit)
      case Kind.BufferNs   => Entry.Buffer(timestamp, constantNs)
    }
  }

  def processState(metricState: MetricState.Untyped): Option[Double] = metricState match {
    case MetricState.Counter(_) => None
    case MetricState.Frequency(_) => None
    case MetricState.Gauge(value) => Some(value)
    case MetricState.Histogram(_, _, _, _, _) => None
    case MetricState.Summary(_, _, _, _, _, _) => None
  }
}
