package dogs

object Tokenizer {
  def tokenize(s: String): List[String] = s.split("[ \t\r\n]").toList
}
