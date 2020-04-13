package dogs

object Counter {
  def wordCounter(ms: Map[String, Int], words: List[String]): Map[String, Int] =
    words.foldLeft(ms)((msInner: Map[String, Int], w: String) =>
      msInner + (w -> (msInner getOrElse (w, 1)))
    )
}
