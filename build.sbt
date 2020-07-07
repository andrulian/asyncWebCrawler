name := "async_webcrawler"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies ++=
  Seq(
    "com.typesafe.akka" %% "akka-http"   % "10.1.12",
    "com.typesafe.akka" %% "akka-stream" % "2.5.26",
    "net.ruippeixotog" %% "scala-scraper" % "2.2.0",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",
    "org.scalactic" %% "scalactic" % "3.1.2",
    "org.scalatest" %% "scalatest" % "3.1.2" % "test"
  )
