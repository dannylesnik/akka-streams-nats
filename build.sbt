name := "akka-streams-nats"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "io.nats" % "java-nats-streaming" % "2.2.3"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test
