package bpe

import scala.util.matching.Regex

object Tokenizer {
  val splitRegex: Regex = "[ \t\r\n]".r

  def tokenize(s: String): List[String] = splitRegex.split(s).toList.filter(_.nonEmpty)
}
