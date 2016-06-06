name := "test-cv"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    "com.sumologic.elasticsearch" % "elasticsearch-core" % "1.0.14",
    "com.sumologic.elasticsearch" % "elasticsearch-aws" % "1.0.14",
    "io.spray" %%  "spray-json" % "1.3.2"
)
