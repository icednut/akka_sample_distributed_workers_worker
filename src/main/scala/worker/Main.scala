package worker

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import backend.WorkManagerSingleton
import work.Worker

import scala.concurrent.duration._

/**
 * @author will.109
 * @date 2020/02/19
 **/
object Main {
  val workerPortRange = 5000 to 9999

  def main(args: Array[String]): Unit = {
    val port = getPort(args.headOption)
    val config = ConfigFactory.parseString(
      s"""
      akka.remote.artery.canonical.port=$port
    """).withFallback(ConfigFactory.load())

    ActorSystem[Nothing](Guardian(), "ClusterSystem", config)
  }

  private def getPort(maybePort: Option[String]): Int = maybePort match {
    case Some(portString) if portString.matches("""\d+""") =>
      val port = portString.toInt
      if (workerPortRange.contains(port)) {
        port
      } else {
        workerPortRange.head
      }
    case _ => workerPortRange.head
  }
}

object Guardian {
  def apply(workers: Int = 2): Behavior[Nothing] = {
    Behaviors.setup[Nothing](ctx => {
      val cluster = Cluster(ctx.system)
      val workManagerProxy = WorkManagerSingleton.init(ctx.system)
      (1 to workers).foreach(n => ctx.spawn(Worker(workManagerProxy), s"worker-$n"))
      Behaviors.empty
    })
  }
}
