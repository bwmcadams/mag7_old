name := "mag7"

organization := "net.evilmonkeylabs"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")
                  
libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.12.1" % "test",
  // For testing BSON ,etc against baseline
  "org.mongodb" % "mongo-java-driver" % "2.9.1" % "test",
  "junit" % "junit" % "4.7" % "test"
) 