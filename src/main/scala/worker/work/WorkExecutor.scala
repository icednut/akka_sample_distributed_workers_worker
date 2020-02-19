package worker.work

import java.util.concurrent.ThreadLocalRandom

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import worker.work.Worker.WorkComplete

import scala.concurrent.duration._

/**
 * @author will.109
 * @date 2020/02/19
 **/
object WorkExecutor {
  case class ExecuteWork(n: Int, replyTo: ActorRef[WorkComplete])

  def apply(): Behavior[ExecuteWork] = {
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { doWork =>
        val n = doWork.n
        val n2 = n * n
        val result = s"$n * $n = $n2"

        // simulate that the processing time varies
        val randomProcessingTime =
          ThreadLocalRandom.current.nextInt(1, 3).seconds

        ctx.scheduleOnce(
          randomProcessingTime,
          doWork.replyTo,
          WorkComplete(result)
        )
        Behaviors.same
      }
    }
  }
}
