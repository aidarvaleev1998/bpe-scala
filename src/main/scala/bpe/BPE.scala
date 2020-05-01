package bpe

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.RunnableGraph

import scala.util.{Failure, Success}

trait BuildableWithVocab {
  def build(vocab: Vector[String], config: Config): RunnableGraph[Future[IOResult]]
}

object BPE {
  def run(config: Config): Unit = {
    implicit val system: ActorSystem          = ActorSystem("bpe")
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    def runWithVocab(cmd: BuildableWithVocab): Unit =
      Vocab
        .load(config)
        .run()
        .onComplete {
          case Success(value) =>
            cmd
              .build(value, config)
              .run()
              .onComplete(_ => system.terminate())
          case Failure(exception) =>
            println(exception)
            system.terminate()
        }

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
      case "encode" =>
        runWithVocab(Encode)
      case "" =>
        println(s"${Console.RED}Error: Command to run is not chosen")
        system.terminate()
      case x =>
        println(s"${Console.RED}Error: Invalid command $x")
        system.terminate()
    }
  }
}
