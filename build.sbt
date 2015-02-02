sonatypeSettings

organization := "com.ibm"

name := "couchdb-scala"

version := "0.5.0-SNAPSHOT"

scalaVersion := "2.11.5"

description := "A purely functional Scala client for CouchDB"

homepage := Some(url("https://github.com/beloglazov/couchdb-scala"))

licenses := Seq("The Apache Software License, Version 2.0"
  -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

libraryDependencies ++= Seq(
  "org.scalaz"                  %% "scalaz-core"                 % "7.1.0",
  "org.scalaz"                  %% "scalaz-effect"               % "7.1.0",
  "com.github.julien-truffaut"  %% "monocle-core"                % "1.0.1",
  "com.github.julien-truffaut"  %% "monocle-macro"               % "1.0.1",
  "com.lihaoyi"                 %% "upickle"                     % "0.2.6-RC1",
  "org.http4s"                  %% "http4s-core"                 % "0.6.0",
  "org.http4s"                  %% "http4s-client"               % "0.6.0",
  "org.http4s"                  %% "http4s-blazeclient"          % "0.6.0",
  "org.log4s"                   %% "log4s"                       % "1.1.3",
  "org.specs2"                  %% "specs2"                      % "2.4.16" % "test",
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
  "-Ywarn-unused-import"
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

unmanagedSourceDirectories in Compile += baseDirectory.value / "examples" / "src" / "main" / "scala"

initialCommands in console := "import scalaz._, Scalaz._"

initialCommands in console in Test := "import scalaz._, Scalaz._, scalacheck.ScalazProperties._, scalacheck.ScalazArbitrary._,scalacheck.ScalaCheckBinding._"

logBuffered := false


publishMavenStyle := true

publishArtifact in Test := false

pomExtra := {
    <organization>
      <name>IBM Corporation</name>
      <url>http://www.ibm.com</url>
    </organization>
    <scm>
      <connection>scm:git:git@github.com:beloglazov/couchdb-scala.git</connection>
      <developerConnection>scm:git:git@github.com:beloglazov/couchdb-scala.git</developerConnection>
      <url>https://github.com/beloglazov/couchdb-scala</url>
    </scm>
    <developers>
      <developer>
        <id>beloglazov</id>
        <name>Anton Beloglazov</name>
        <email>anton.beloglazov@gmail.com</email>
        <url>http://beloglazov.info</url>
      </developer>
    </developers>
}
