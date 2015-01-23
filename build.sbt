organization := "com.ibm"

name := "couchdb-scala"

version := "0.5.0-SNAPSHOT"

scalaVersion := "2.11.5"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.scalaz"                  %% "scalaz-core"                 % "7.1.0",
  "org.scalaz"                  %% "scalaz-effect"               % "7.1.0",
  "com.github.julien-truffaut"  %% "monocle-core"                % "1.0.1",
  "com.github.julien-truffaut"  %% "monocle-macro"               % "1.0.1",
  "com.lihaoyi"                 %% "upickle"                     % "0.2.6-RC1",
  "org.http4s"                  %% "http4s-core"                 % "0.5.4",
  "org.http4s"                  %% "http4s-client"               % "0.5.4",
  "org.http4s"                  %% "http4s-blazeclient"          % "0.5.4",
  "org.log4s"                   %% "log4s"                       % "1.1.3",
  "org.specs2"                  %% "specs2"                      % "2.4.15" % "test",
  "org.typelevel"               %% "scalaz-specs2"               % "0.3.0"  % "test",
  "org.scalacheck"              %% "scalacheck"                  % "1.12.1" % "test",
  "org.scalaz"                  %% "scalaz-scalacheck-binding"   % "7.1.0"  % "test",
  "ch.qos.logback"              %  "logback-classic"             % "1.1.2"  % "test"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import" // 2.11 only
)

wartremoverErrors in (Compile, compile) ++= Seq(
  Wart.Any,
  Wart.Any2StringAdd,
  Wart.EitherProjectionPartial,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Serializable,
  Wart.ListOps
)


lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle


lazy val testScalastyle = taskKey[Unit]("testScalastyle")

testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value

(test in Test) <<= (test in Test) dependsOn testScalastyle


testFrameworks := Seq(TestFrameworks.Specs2, TestFrameworks.ScalaCheck)

parallelExecution in Test := false

testOptions in Test += Tests.Argument(
  TestFrameworks.ScalaCheck, "-maxSize", "5", "-minSuccessfulTests", "33", "-workers", "1")

unmanagedSourceDirectories in Compile += baseDirectory.value / "examples" / "src" / "main" / "scala"

initialCommands in console := "import scalaz._, Scalaz._"

initialCommands in console in Test := "import scalaz._, Scalaz._, scalacheck.ScalazProperties._, scalacheck.ScalazArbitrary._,scalacheck.ScalaCheckBinding._"

logBuffered := false

lazy val buildSettings = Seq(
  organization := organization.value,
  version := version.value,
  scalaVersion := scalaVersion.value
)

val app = (project in file("."))
  .settings(buildSettings: _*)
  .settings(assemblySettings: _*)
  .disablePlugins(plugins.JUnitXmlReportPlugin)

