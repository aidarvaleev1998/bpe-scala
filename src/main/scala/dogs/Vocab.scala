package dogs

object Vocab {
  def getWordNgrams(word: String, config: BPEConfig): List[String] = {
    val w = word.toList :+ config.eow
    for {
      ngramLen <- 1 to config.ngramMax
      ngram <- w.iterator.sliding(ngramLen)
    } yield ngram.mkString(" ")
  }.toList

  def build(wordCounts: Map[String, Int], config: BPEConfig): List[String] = {
    wordCounts
      .map{ case (w, c) =>
        getWordNgrams(w, config).map(w => (w, c))
    }.foldLeft(Map[String, Int]())((ms, wordNgrams) => {
      wordNgrams.foldLeft(ms)((msInner, ngramCount) => {
        val (ngram: String, count: Int) = ngramCount
        val v = (msInner getOrElse (ngram, 0)) + count
        msInner + (ngram -> v)
      })
    }).toList
      .filter(x => x._2 > config.minCount)
      .sortWith((l, r) =>{
        val (_, c1: Int) = l
        val (_, c2: Int) = r
        c1 > c2
      }).map(_._1).take(config.vocabSize)}
}
