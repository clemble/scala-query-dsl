name := "scala-query-dsl"

version := "0.0.1"

scalaVersion := "2.11.8"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",

  libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "3.7" % "test"
  )
)

lazy val core = project.
  settings(commonSettings: _*).
  settings()

lazy val query = project.
  settings(commonSettings: _*).
  dependsOn(core % "compile->compile;test->test")

lazy val memory = project.
  settings(commonSettings: _*).
  dependsOn(core % "compile->compile;test->test")

lazy val mongo = project.
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.5.6"
    )
  ).dependsOn(core % "compile->compile;test->test")