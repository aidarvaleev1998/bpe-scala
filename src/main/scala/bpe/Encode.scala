package bpe

import java.nio.file.Paths

import scala.concurrent.Future
import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph}
import akka.util.ByteString

object Encode extends BuildableWithVocab {
  def encode(token2id: Map[String, Int], config: Config): Flow[String, String, NotUsed] = {
    def join(acc: String, ch: String): String =
      if (acc.isEmpty) ch else acc + " " + ch

    Flow[String]
      .map(Tokenizer.tokenize)
      .map(line => line.flatMap(w => w.toList :+ config.eowToken))
      .map(line =>
        (line :+ "")
          .scanLeft(("", false, 0)){
            case ((acc, _, _), ch) if token2id.contains(join(acc, ch.toString)) =>
              (join(acc, ch.toString), false, 0)
            case ((acc, _, _), ch) if token2id.contains(acc) && ch.toString.isEmpty =>
              ("", true, token2id.getOrElse(acc, 0))
            case ((acc, _, _), ch) if token2id.contains(acc) && token2id.contains(ch.toString) =>
              (ch.toString, true, token2id.getOrElse(acc, 0))
            case ((acc, _, _), _) if token2id.contains(acc) =>
              ("", true, token2id.getOrElse(acc, 0))
            case ((_, _, _), _) =>
              ("", true, 0)
          }
          .filter(_._2)
          .map(_._3)
          .mkString(" ")
      )
  }

  def build(vocab: Vector[String], config: Config): RunnableGraph[Future[IOResult]] = {
    val token2id: Map[String, Int] = vocab.zipWithIndex.toMap
    FileIO
      .fromPath(Paths.get(config.inputFile))
      .mapConcat(_.utf8String.split("\n"))
      .via(encode(token2id, config))
      .map(t => ByteString(t + "\n"))
      .toMat(FileIO.toPath(Paths.get(config.outputFile)))(Keep.right)
  }
}
