name := "SparkHW"

version := "1.0"

scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.3",
  "org.apache.spark" %% "spark-sql" % "3.5.3",
  "io.circe" %% "circe-core" % "0.14.10",
  "io.circe" %% "circe-generic" % "0.14.10",
  "io.circe" %% "circe-parser" % "0.14.10",
  "com.softwaremill.sttp.client3" %% "core" % "3.10.1" exclude("org.typelevel", "cats-core_2.12"),
  "com.softwaremill.sttp.client3" %% "circe" % "3.10.1" exclude("org.typelevel", "cats-core_2.12"),
  "org.typelevel" %% "cats-core" % "2.12.0",
  //"com.softwaremill.sttp.client3" %% "core" % "3.9.0",
  //"com.softwaremill.sttp.client3" %% "circe" % "3.9.0",
  "ch.hsr" % "geohash" % "1.4.0", //  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)


Compile / mainClass := Some("SparkJob")


// merge thing
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _                            => MergeStrategy.first
}