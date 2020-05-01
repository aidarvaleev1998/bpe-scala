# BPE on Scala

This repository provides an implementation of Byte-Pair Encoding on Scala.

## Usage from sbt shell

```bash
run --help
run build -i data.ru -v data.vocab
run encode -i data.ru -v data.vocab -o data.out
run decode -i data.out -v data.vocab -o data.out2
```
