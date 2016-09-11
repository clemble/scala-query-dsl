name := "scala-query-dsl"

version := "0.0.1"

scalaVersion := "2.11.8"

lazy val core = project.
  settings()

lazy val query = project.
  settings().
  dependsOn(core % "compile->compile;test->test")

lazy val memory = project.
  settings().
  dependsOn(core % "compile->compile;test->test")

lazy val mongo = project.
  settings(
    libraryDependencies += "org.reactivemongo" % "reactivemongo_2.11" % "0.11.14"
  ).dependsOn(core % "compile->compile;test->test")
