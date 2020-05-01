package bpe

import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem

import scala.util.{Failure, Success}

object BPE {
  def run(config: Config): Unit = {
    implicit val system: ActorSystem          = ActorSystem("bpe")
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    config.mode match {
      case "build" =>
        Vocab
          .build(config)
          .run()
          .onComplete {
            case Success(_) =>
              system.terminate()
            case Failure(e) =>
              println(e)
              system.terminate()
          }
      case "" =>
        println(s"${Console.RED}Error: Command to run is not chosen")
        system.terminate()
      case x =>
        println(s"${Console.RED}Error: Invalid command $x")
        system.terminate()
    }
  }
}
