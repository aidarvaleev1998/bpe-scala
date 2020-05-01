package bpe

import java.nio.file.Paths

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, RunnableGraph}

import scala.util.{Failure, Success}

trait BuildableWithVocab {
  def build(vocab: Vector[String], config: Config): RunnableGraph[Future[IOResult]]
}

object BPE {
  def run(config: Config): Unit = {
    implicit val system: ActorSystem          = ActorSystem("bpe")
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    config.mode match {
      case "build" =>
        Vocab
          .build(config)
          .runWith(FileIO.toPath(Paths.get(config.vocabFile)))
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
