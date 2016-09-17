name := "scala-query-dsl"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.7" % "test",

  "ch.qos.logback" % "logback-classic" % "1.1.7" % "provided",
  "org.reactivemongo" %% "reactivemongo" % "0.11.14" % "provided",
  "org.reactivemongo" %% "reactivemongo-play-json" % "0.11.14" % "provided",
  "org.reactivemongo" %% "reactivemongo-iteratees" % "0.11.14" % "provided",
  "com.typesafe.play" %% "play-json" % "2.5.6" % "provided"
)