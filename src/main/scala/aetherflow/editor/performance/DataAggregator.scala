package aetherflow.editor.performance

//import scalafx.beans.property.ObjectProperty
import aetherflow.engine.*
import zio.*

class DataAggregator {
  import performance.Data.DataAggregator.*
  
//  val windowState = ObjectProperty[Iterable[Entry]](Iterable.empty[Entry])

  def aggregate(newEntries: Iterable[Entry]): Unit = {
//    windowState.value = {
//      val (constants, others) = (windowState.value ++ newEntries).partition {
//        case _: Entry.Constant => true
//        case _ => false
//      }
//      val distinctConstants = constants
//        .asInstanceOf[Iterable[Entry.Constant]]
//        .groupBy(_.header.asMetricName)
//        .flatMap { case (_, group) => group.lastOption }
//      distinctConstants ++ others
//    }
  }
}
object DataAggregator {
  def layer = ZLayer.succeed(new DataAggregator)
}
