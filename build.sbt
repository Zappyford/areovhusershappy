lazy val root = (project in file(".")).
  settings(
    name := "isovhsupporthappy",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in Compile := Some("org.zappy.ovh.happiness")
  )

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.9"
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.7"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => (assemblyMergeStrategy in assembly).value(x)
}