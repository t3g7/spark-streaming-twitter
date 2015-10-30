lazy val root = (project in file(".")).
  settings(
    name := "spark-streaming-test",
    version := "1.0",
    scalaVersion := "2.11.7",
    mainClass in Compile := Some("Test")
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.5.1" % "provided"
)
