import play.PlayImport._
import play.PlayScala
import sbtassembly.Plugin.AssemblyKeys._

name := "VodQA-Whiskey"

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

net.virtualvoid.sbt.graph.Plugin.graphSettings

assemblySettings

jarName in assembly := "VodQABomb.jar"

mainClass in assembly := Some("play.core.server.NettyServer")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "JCenter repo" at "https://bintray.com/bintray/jcenter/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

mergeStrategy in assembly := {
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.last
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  ws exclude("commons-logging", "commons-logging"),
  cache,
  "de.leanovate.play-mockws" %% "play-mockws" % "0.12" % "test"
)

