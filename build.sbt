lazy val root = (project in file(".")).
  settings(
    name := "spark-streaming-twitter",
    version := "0.2",
    scalaVersion := "2.10.4",
    mainClass in Compile := Some("TwitterStreamingApp")
  )

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "org.apache.spark" %% "spark-core" % "1.5.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.5.1" % "provided",
  "org.apache.spark" %% "spark-streaming-twitter" % "1.5.1",
  "org.twitter4j" % "twitter4j-core" % "3.0.6",
  "org.twitter4j" % "twitter4j-stream" % "3.0.6",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.5.0-M2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// META-INF discarding
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}