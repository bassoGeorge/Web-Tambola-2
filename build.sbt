name := """Web-Tambola-2"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3-M1",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "requirejs" % "2.1.11-1",
  "org.webjars" % "jquery" % "1.9.1",
  "org.webjars" % "angularjs" % "1.3.0-beta.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3",
  "com.typesafe.slick" %% "slick" % "2.1.0-M2",
  "com.h2database" % "h2" % "1.3.175",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

lazy val root = (project in file(".")).addPlugins(PlayScala)
