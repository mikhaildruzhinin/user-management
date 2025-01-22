This / version := "0.1.0-SNAPSHOT"
This / organization := "ru.mikhaildruzhinin"
This / scalaVersion := "2.13.15"

val tapirVersion = "1.11.12"
val circeVersion = "0.14.10"
val jwtScalaVersion = "10.0.1"
val logbackVersion = "1.5.16"
val scalatestVersion = "3.2.19"
val sttpCirceVersion = "3.10.2"
val pureconfigVersion = "0.17.8"

lazy val root = (project in file("."))
  .settings(
    name := "user-management",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
      "com.github.jwt-scala" %% "jwt-circe" % jwtScalaVersion,
      "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "com.softwaremill.sttp.client3" %% "circe" % sttpCirceVersion % Test
    )
  )
