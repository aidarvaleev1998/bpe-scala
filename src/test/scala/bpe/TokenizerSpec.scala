package bpe

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TokenizerSpec extends AnyFlatSpec with Matchers {
  "tokenize" should "split text" in {
    Tokenizer.tokenize("I am doing project") shouldBe List("I", "am", "doing", "project")
    Tokenizer.tokenize("1 2\t3") shouldBe List("1", "2", "3")
    Tokenizer.tokenize("\t1 2\t ") shouldBe List("1", "2")
  }
}
