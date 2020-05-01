package bpe

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DecodeSpec extends AnyFlatSpec with Matchers {
  "encode" should "map tokens to indices" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val vocab = Vector("<eow>", "c", "b", "a", "d", "c <eow>",
      "c c", "c c <eow>", "b <eow>", "b b <eow>", "b b", "d d",
      "a <eow>", "a a <eow>", "a a", "e")
      .zipWithIndex.map { case (v, k) => (k, v) }.toMap
    val (pub, sub) =
      TestSource
        .probe[String]
        .via(Decode.decode(vocab, Config()))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(n = 3)
    pub.sendNext("13 13 13 9 9 7 7")
    pub.sendNext("9 9 7 7 7 11 4 0 11 4 0")
    pub.sendNext("15 15 15 0")
    pub.sendComplete()
    sub.expectNext("aa aa aa bb bb cc cc")
    sub.expectNext("bb bb cc cc cc ddd ddd")
    sub.expectNext("eee")
    sub.expectComplete()

    system.terminate()
  }
}
