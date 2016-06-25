import xerial.sbt.Sonatype.SonatypeKeys._

sonatypeSettings

profileName := "com.ibm.couchdb-scala"

organization := "com.ibm"

name := "couchdb-scala"

version := "0.8.0-SNAPSHOT"

scalaVersion := "2.11.8"

description := "A purely functional Scala client for CouchDB"

homepage := Some(url("https://github.com/beloglazov/couchdb-scala"))

licenses := Seq("The Apache Software License, Version 2.0"
  -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

libraryDependencies ++= Seq(
  "org.scalaz"                  %% "scalaz-core"                 % "7.2.1",
  "org.scalaz"                  %% "scalaz-effect"               % "7.2.1",
  "org.http4s"                  %% "http4s-core"                 % "0.13.0a",
  "org.http4s"                  %% "http4s-client"               % "0.13.0a",
  "org.http4s"                  %% "http4s-blaze-client"         % "0.13.0a",
  "com.lihaoyi"                 %% "upickle"                     % "0.4.1",
  "com.github.julien-truffaut"  %% "monocle-core"                % "1.2.0",
  "com.github.julien-truffaut"  %% "monocle-macro"               % "1.2.0",
  "org.log4s"                   %% "log4s"                       % "1.2.1",
  "org.specs2"                  %% "specs2"                      % "3.7" % "test",
  "org.typelevel"               %% "scalaz-specs2"               % "0.3.0"  % "test",
  "org.scalacheck"              %% "scalacheck"                  % "1.13.0" % "test",
  "org.scalaz"                  %% "scalaz-scalacheck-binding"   % "7.2.1"  % "test",
  "ch.qos.logback"              %  "logback-classic"             % "1.1.6"  % "test"
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

scalacOptions in (Compile, console) ~= (_ filterNot (
  List("-Ywarn-unused-import", "-Xfatal-warnings").contains(_)))

wartremover.wartremoverSettings

wartremover.wartremoverErrors in (Compile, compile) ++= Seq(
  wartremover.Wart.Any,
  wartremover.Wart.Any2StringAdd,
  wartremover.Wart.EitherProjectionPartial,
  wartremover.Wart.OptionPartial,
  wartremover.Wart.Product,
  wartremover.Wart.Serializable,
  wartremover.Wart.ListOps
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

initialCommands in console := "import scalaz._, Scalaz._, com.ibm.couchdb._"

initialCommands in console in Test := "import scalaz._, Scalaz._, scalacheck.ScalazProperties._, " +
                                      "scalacheck.ScalazArbitrary._,scalacheck.ScalaCheckBinding._"

logBuffered := false

publishMavenStyle := true

publishArtifact in Test := false

pomExtra := {
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
