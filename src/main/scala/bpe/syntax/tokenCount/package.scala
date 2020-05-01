package bpe.syntax

package object tokenCount {
  implicit class SyntaxTokenCount(private val inner: (String, Int)) extends AnyVal {
    def token: String = inner._1
    def count: Int    = inner._2
  }

}
