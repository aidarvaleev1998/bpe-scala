package bpe

import java.nio.file.Paths

import scala.concurrent.Future
import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph, Sink}
import akka.util.ByteString
import bpe.syntax.tokenCount._

object Vocab {
  def wordToNgrams(config: Config): Flow[Map[String, Int], (String, Int), NotUsed] = {
    def getWordNgrams(word: String): List[String] = {
      val w = word.toList :+ config.eowToken
      for {
        ngramLen <- 1 to config.maxNgramLen
        ngram    <- w.iterator.sliding(ngramLen)
      } yield ngram.mkString(" ")
    }.toList

    Flow[Map[String, Int]]
      .mapConcat(_.toList)
      .mapConcat(wordCount =>
        getWordNgrams(wordCount.token)
          .map((_, wordCount.count))
      )
  }

  def sortByCount(minCount: Int, unkToken: String): Flow[Map[String, Int], String, NotUsed] =
    Flow[Map[String, Int]]
      .mapConcat(
        unkToken +: _.toList
          .filter(_.count >= minCount)
          .sortBy(_.count)
          .reverse
          .map(_.token)
      )

  def computeVocab(config: Config): Flow[String, String, NotUsed] =
    Flow[String]
      .mapConcat(Tokenizer.tokenize)
      .map((_, 1))
      .via(Counter.counter)
      .via(wordToNgrams(config))
      .via(Counter.counter)
      .via(sortByCount(config.minCount, config.unkToken))
      .take(config.vocabSize)

  def build(config: Config): RunnableGraph[Future[IOResult]] = {
    FileIO
      .fromPath(Paths.get(config.inputFile))
      .map(_.utf8String)
      .via(computeVocab(config))
      .map(t => ByteString(t + "\n"))
      .toMat(FileIO.toPath(Paths.get(config.vocabFile)))(Keep.right)
  }

  def load(config: Config): RunnableGraph[Future[Vector[String]]] =
    FileIO
      .fromPath(Paths.get(config.vocabFile))
      .mapConcat(_.utf8String.split("\n"))
      .toMat(Sink.collection[String, Vector[String]])(Keep.right)
}
