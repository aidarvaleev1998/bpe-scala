package bpe

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EncodeSpec extends AnyFlatSpec with Matchers {
  "encode" should "map tokens to indices" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val vocab = Vector("<eow>", "c", "b", "a", "d", "c <eow>",
      "c c", "c c <eow>", "b <eow>", "b b <eow>", "b b", "d d",
      "a <eow>", "a a <eow>", "a a", "e")
      .zipWithIndex.toMap
    val (pub, sub) =
      TestSource
        .probe[String]
        .via(Encode.encode(vocab, Config()))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(n = 3)
    pub.sendNext("aa aa aa bb bb cc cc")
    pub.sendNext("bb bb cc cc cc ddd ddd")
    pub.sendNext("eee")
    pub.sendComplete()
    sub.expectNext("13 13 13 9 9 7 7")
    sub.expectNext("9 9 7 7 7 11 4 0 11 4 0")
    sub.expectNext("15 15 15 0")
    sub.expectComplete()

    system.terminate()
  }
}
