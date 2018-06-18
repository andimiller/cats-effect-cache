addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

lazy val CatsEffectVersion = "1.0.0-RC2"
lazy val ScalaTestVersion = "3.0.3"
lazy val ScalaCheckVersion = "1.13.4"
lazy val CaffeineVersion = "2.6.2"

lazy val root = (project in file("."))
  .settings(
    organization := "net.andimiller",
    name := "cats-effect-cache",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    scalacOptions := Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-Ypartial-unification"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "com.github.ben-manes.caffeine" % "caffeine" % CaffeineVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % Test
    )
  )
