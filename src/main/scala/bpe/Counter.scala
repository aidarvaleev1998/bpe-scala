package bpe

import akka.NotUsed
import akka.stream.scaladsl.Flow

import bpe.syntax.tokenCount._

object Counter {
  def counter: Flow[(String, Int), Map[String, Int], NotUsed] = {
    Flow[(String, Int)]
      .fold(Map[String, Int]())((acc, tokenCount) => {
        val v = (acc getOrElse (tokenCount.token, 0)) + tokenCount.count
        acc + (tokenCount.token -> v)
      })
  }
}
