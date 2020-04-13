package dogs

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import java.nio.file.Paths

import akka.stream.IOResult
import akka.util.ByteString

import scala.concurrent._
import scala.util.Success

import pureconfig._
import pureconfig.generic.auto._

case class BPEConfig(
                      minCount: Int,
                      ngramMax: Int,
                      vocabSize: Int,
                      eow: String
                    )

object Main extends App {
  ConfigSource.default.load[BPEConfig] match {
    case Right(config) =>
      implicit val system: ActorSystem = ActorSystem("BPE")
      implicit val ec: ExecutionContextExecutor = system.dispatcher

      val source: Source[String, Any] = FileIO
        .fromPath(Paths.get("data.ru"))
        .map(_.utf8String)

      val wordCounts: Future[Map[String, Int]] = source
        .via(Flow[String].map(Tokenizer.tokenize))
        .runWith(Sink.fold(Map[String, Int]())(Counter.wordCounter))

      wordCounts.onComplete {
        case Success(value) =>
          val result: Future[IOResult] =
            Source(Vocab.build(value, config))
            .map(t => ByteString(t + "\n"))
            .runWith(FileIO.toPath(Paths.get("ru.vocab")))
          result.onComplete(_ => system.terminate())
      }
    case Left(e) => println(e)
  }
}
