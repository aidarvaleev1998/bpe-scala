package bpe

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CounterSpec extends AnyFlatSpec with Matchers {
  "counter" should "work" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val (pub, sub) =
      TestSource
        .probe[(String, Int)]
        .via(Counter.counter)
        .toMat(TestSink.probe[Map[String, Int]])(Keep.both)
        .run()

    sub.request(n = 1)
    pub.sendNext(("a", 1))
    pub.sendNext(("b", 1))
    pub.sendNext(("b", 1))
    pub.sendNext(("c", 4))
    pub.sendNext(("a", 2))
    pub.sendComplete()
    sub.expectNext(Map(("a", 3), ("b", 2), ("c", 4)))
    sub.expectComplete()

    system.terminate()
  }
}
