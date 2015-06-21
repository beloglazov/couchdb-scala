resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.13")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")