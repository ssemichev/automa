javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    name := "automa",
    organization := "com.targetedvictory",
    version := "1.0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    retrieveManaged := true,
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "1.0.0"
  )

resolvers ++= Seq(
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.slf4j" % "slf4j-simple" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
)

assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
}