package bpe.syntax.tokenCount

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TokenCountSyntaxSpec extends AnyFlatSpec with Matchers {
  "token" should "work for (String, Int) tuples" in {
    ("a", 1).token shouldBe "a"
    ("b", 2).token shouldBe "b"
  }

  "count" should "work for (String, Int) tuples" in {
    ("a", 1).count shouldBe 1
    ("b", 2).count shouldBe 2
  }
}
