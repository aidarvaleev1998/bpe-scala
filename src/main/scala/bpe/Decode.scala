package bpe

import java.nio.file.Paths

import akka.NotUsed

import scala.concurrent.Future
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph}
import akka.util.ByteString

import scala.util.Try

object Decode extends BuildableWithVocab {
  def decode(id2token: Map[Int, String], config: Config): Flow[String, String, NotUsed] = {
    def tryToInt(s: String) = Try(s.toInt).toOption

    Flow[String]
      .map(Tokenizer.tokenize)
      .map(
        _.map(tryToInt)
          .collect{ case Some(v) => v }
          .map(i => id2token.getOrElse(i, config.unkToken))
          .map(_.replaceAll(" ", ""))
          .map(_.replaceAll(config.eowToken, " "))
          .mkString
          .trim
      )
  }

  def build(vocab: Vector[String], config: Config): RunnableGraph[Future[IOResult]] = {
    val id2token: Map[Int, String] = vocab.zipWithIndex.map { case (v, k) => (k, v) }.toMap

    FileIO
      .fromPath(Paths.get(config.inputFile))
      .mapConcat(_.utf8String.split("\n"))
      .via(decode(id2token, config))
      .map(t => ByteString(t + "\n"))
      .toMat(FileIO.toPath(Paths.get(config.outputFile)))(Keep.right)
  }
}
