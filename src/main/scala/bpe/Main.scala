package bpe

import scopt.OParser

case class Config(
    mode: String = "",
    inputFile: String = "",
    outputFile: String = "data.out",
    vocabFile: String = "data.vocab",
    maxNgramLen: Int = 3,
    minCount: Int = 10,
    vocabSize: Int = 1024,
    eowToken: String = "<eow>",
    unkToken: String = "<unk>",
)

object Main extends App {
  val builder = OParser.builder[Config]
  val parser = {
    import builder._
    OParser.sequence(
      programName("bpe"),
      head("bpe", "0.1"),
      opt[String]('i', "input-file")
        .required()
        .valueName("<path>")
        .action((x, c) => c.copy(inputFile = x))
        .text("path to the input file"),
      opt[String]('v', "vocab-file")
        .required()
        .valueName("<path>")
        .action((x, c) => c.copy(vocabFile = x))
        .text("path to the vocab file"),
      help("help").text("prints this usage text"),
      cmd("build")
        .action((_, c) => c.copy(mode = "build"))
        .text("builds a bpe vocabulary for the input data file")
        .children(
          opt[Int]('m', "max-ngram-len")
            .action((x, c) => c.copy(maxNgramLen = x))
            .validate(x =>
              if (x > 0) success
              else failure("Max ngram length must be positive")
            )
            .text("max possible length for tokens"),
          opt[Int]('c', "min-count")
            .action((x, c) => c.copy(minCount = x))
            .validate(x =>
              if (x > 0) success
              else failure("Min count must be positive")
            )
            .text("min acceptable count for tokens"),
          opt[Int]('n', "vocab-size")
            .action((x, c) => c.copy(vocabSize = x))
            .validate(x =>
              if (x > 0) success
              else failure("Vocab size must be positive")
            )
            .text("vocabulary size - max number of tokens"),
          opt[String]('e', "eow")
            .action((x, c) => c.copy(eowToken = x))
            .text("eow - end of word token"),
          opt[String]('u', "unk")
            .action((x, c) => c.copy(unkToken = x))
            .text("unk - unknown token"),
        ),
      cmd("encode")
        .action((_, c) => c.copy(mode = "encode"))
        .text("encodes an input data file with the given bpe vocabulary")
        .children(
          opt[String]('o', "output-file")
            .required()
            .valueName("<path>")
            .action((x, c) => c.copy(outputFile = x))
            .text("path to the output file"),
        ),
      cmd("decode")
        .action((_, c) => c.copy(mode = "decode"))
        .text("decodes an encoded input file with the given bpe vocabulary")
        .children(
          opt[String]('o', "output-file")
            .required()
            .valueName("<path>")
            .action((x, c) => c.copy(outputFile = x))
            .text("path to the output file"),
        )
    )
  }

  OParser.parse(parser, args, Config()) match {
    case Some(config) => BPE.run(config)
    case _            => ()
  }
}
