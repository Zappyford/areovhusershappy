enablePlugins(UniversalPlugin)

name := "areovhusershappy"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0" % "provided"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.9"
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.7"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"

mainClass in Compile := Some("org.zappy.ovh.happiness")


// removes all jar mappings in universal and appends the fat jar
mappings in Universal := {
  // universalMappings: Seq[(File,String)]
  val universalMappings = (mappings in Universal).value
  val fatJar = (assembly in Compile).value
  // removing means filtering
  val filtered = universalMappings filter {
    case (_, name) =>  ! name.endsWith(".jar")
  }

  // add the fat jar
  filtered :+ (fatJar -> ("lib/" + fatJar.getName))
}


mappings in Universal += {
  //add the application.conf
  val conf = (resourceDirectory in Compile).value / "application.conf"
  conf -> "conf/application.conf"
}

mappings in Universal += {
  // add the log4j.properties
  val conf = (resourceDirectory in Compile).value / "log4j.properties"
  conf -> "conf/log4j.properties"
}

mappings in Universal += {
  // add the English stopwords
  val conf = (resourceDirectory in Compile).value / "English_Stopwords.txt"
  conf -> "conf/English_Stopwords.txt"
}

