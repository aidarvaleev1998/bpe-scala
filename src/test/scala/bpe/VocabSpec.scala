package bpe

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VocabSpec extends AnyFlatSpec with Matchers {
  "wordToNgrams" should "generate ngrams with max ngram len 1" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val config = Config(maxNgramLen = 1)
    val eow    = config.eowToken
    val (pub, sub) =
      TestSource
        .probe[Map[String, Int]]
        .via(Vocab.wordToNgrams(config))
        .toMat(TestSink.probe[(String, Int)])(Keep.both)
        .run()

    sub.request(n = 8)
    pub.sendNext(Map(("abc", 3), ("bla", 2)))
    pub.sendComplete()
    sub.expectNextUnordered(("a", 3), ("b", 3), ("c", 3), (eow, 3), ("b", 2), ("l", 2), ("a", 2), (eow, 2))
    sub.expectComplete()

    system.terminate()
  }

  "wordToNgrams" should "generate ngrams with max ngram len 2" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val config = Config(maxNgramLen = 2)
    val eow    = config.eowToken
    val (pub, sub) =
      TestSource
        .probe[Map[String, Int]]
        .via(Vocab.wordToNgrams(config))
        .toMat(TestSink.probe[(String, Int)])(Keep.both)
        .run()

    sub.request(n = 14)
    pub.sendNext(Map(("abc", 3), ("bla", 2)))
    pub.sendComplete()
    sub.expectNextUnordered(
      ("a", 3),
      ("b", 3),
      ("c", 3),
      (eow, 3),
      ("a b", 3),
      ("b c", 3),
      (s"c $eow", 3),
      ("b", 2),
      ("l", 2),
      ("a", 2),
      (eow, 2),
      ("b l", 2),
      ("l a", 2),
      (s"a $eow", 2)
    )
    sub.expectComplete()

    system.terminate()
  }

  "wordToNgrams" should "generate ngrams with max ngram len 3" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val config = Config()
    val eow    = config.eowToken
    val (pub, sub) =
      TestSource
        .probe[Map[String, Int]]
        .via(Vocab.wordToNgrams(config))
        .toMat(TestSink.probe[(String, Int)])(Keep.both)
        .run()

    sub.request(n = 18)
    pub.sendNext(Map(("abc", 3), ("bla", 2)))
    pub.sendComplete()
    sub.expectNextUnordered(
      ("a", 3),
      ("b", 3),
      ("c", 3),
      (eow, 3),
      ("a b", 3),
      ("b c", 3),
      (s"c $eow", 3),
      ("a b c", 3),
      (s"b c $eow", 3),
      ("b", 2),
      ("l", 2),
      ("a", 2),
      (eow, 2),
      ("b l", 2),
      ("l a", 2),
      (s"a $eow", 2),
      ("b l a", 2),
      (s"l a $eow", 2)
    )
    sub.expectComplete()

    system.terminate()
  }

  "sortByCount" should "sort tokens by count with mincount = 3" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val (pub, sub) =
      TestSource
        .probe[Map[String, Int]]
        .via(Vocab.sortByCount(3))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(n = 3)
    pub.sendNext(Map(("e", 20), ("bcaa", 2), ("c", 4), ("a", 21)))
    pub.sendComplete()
    sub.expectNext("a")
    sub.expectNext("e")
    sub.expectNext("c")
    sub.expectComplete()

    system.terminate()
  }

  "sortByCount" should "sort tokens by count with mincount = 21" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val (pub, sub) =
      TestSource
        .probe[Map[String, Int]]
        .via(Vocab.sortByCount(21))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(2)
    pub.sendNext(Map(("e", 20), ("bcaa", 2), ("c", 4), ("a", 21)))
    pub.sendComplete()
    sub.expectNext("a")
    sub.expectComplete()

    system.terminate()
  }

  "computeVocab" should "generate vocabulary" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val config = Config(minCount = 2)
    val eow    = config.eowToken
    val (pub, sub) =
      TestSource
        .probe[String]
        .via(Vocab.computeVocab(config))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(20)
    pub.sendNext("aa aa aa bb bb cc cc")
    pub.sendNext("bb bb cc cc cc ddd ddd")
    pub.sendNext("eee")
    pub.sendComplete()
    sub.expectNext(eow)
    sub.expectNext("c")
    sub.expectNext("b")
    sub.expectNextUnordered("a", "d")
    sub.expectNextUnordered(s"c $eow", "c c", s"c c $eow")
    sub.expectNextUnordered(s"b $eow", s"b b $eow", "b b", "d d")
    sub.expectNextUnordered(s"a $eow", s"a a $eow", "a a", "e")
    sub.expectNextUnordered(s"d $eow", s"d d $eow", "d d d", "e e")
    sub.expectComplete()

    system.terminate()
  }

  "computeVocab" should "generate vocabulary limited by vocab size" in {
    implicit val system: ActorSystem = ActorSystem("test")

    val config = Config(minCount = 2, vocabSize = 16)
    val eow    = config.eowToken
    val (pub, sub) =
      TestSource
        .probe[String]
        .via(Vocab.computeVocab(config))
        .toMat(TestSink.probe[String])(Keep.both)
        .run()

    sub.request(16)
    pub.sendNext("aa aa aa bb bb cc cc")
    pub.sendNext("bb bb cc cc cc ddd ddd")
    pub.sendNext("eee")
    pub.sendComplete()
    sub.expectNext(eow)
    sub.expectNext("c")
    sub.expectNext("b")
    sub.expectNextUnordered("a", "d")
    sub.expectNextUnordered(s"c $eow", "c c", s"c c $eow")
    sub.expectNextUnordered(s"b $eow", s"b b $eow", "b b", "d d")
    sub.expectNextUnordered(s"a $eow", s"a a $eow", "a a", "e")
    sub.expectComplete()

    system.terminate()
  }
}
