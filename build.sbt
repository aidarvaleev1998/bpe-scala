name := "bpe-scala"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.1" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.3" % Test,
  "com.github.scopt" %% "scopt" % "4.0.0-RC2"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
