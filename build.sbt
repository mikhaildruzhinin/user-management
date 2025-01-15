val tapirVersion = "1.11.12"
val circeVersion = "0.14.10"
val jwtScalaVersion = "10.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "user-management",
    version := "0.1.0-SNAPSHOT",
    organization := "ru.mikhaildruzhinin",
    scalaVersion := "2.13.15",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.16",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
      "com.github.jwt-scala" %% "jwt-circe" % jwtScalaVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.10.2" % Test
    )
  )
