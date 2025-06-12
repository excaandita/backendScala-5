name := """meeting-v"""
organization := "id.dojo"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  cacheApi,
  ehcache, // or cacheApi
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "org.playframework.anorm" %% "anorm-postgres" % "2.7.0",
  "com.typesafe.play" %% "play-json" % "2.10.6"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "id.dojo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "id.dojo.binders._"
