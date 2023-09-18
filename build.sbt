ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaFXStarter"
  )

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8") //, "-Ymacro-annotations")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.last//first
}

Compile / resourceDirectory := (Compile / scalaSource).value
libraryDependencies ++= Seq(
  "org.scalafx" % "scalafx_2.12" % "20.0.0-R31",
  "org.scalafx" % "scalafxml-core-sfx8_2.12" % "0.5",
  // https://mvnrepository.com/artifact/org.scalaj/scalaj-http
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "net.liftweb" %% "lift-json" % "3.0.1",
  "musajixelinterface" %% "musajixelinterface" % "0.1.0-SNAPSHOT",
  // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
  "org.apache.logging.log4j" % "log4j-core" % "2.19.0",
  "com.beachape" %% "enumeratum" % "1.7.2"

)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies ++= Seq(

  // Start with this one
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",

  // And add any of these as needed
  "org.tpolecat" %% "doobie-h2" % "1.0.0-RC1", // H2 driver 1.4.200 + type mappings.
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1", // Postgres driver 42.3.1 + type mappings.
  "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC1" % "test", // Specs2 support for typechecking statements.
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC1" % "test" // ScalaTest support for typechecking statements.

)

resolvers ++= Opts.resolver.sonatypeOssSnapshots
resolvers += Resolver.mavenLocal

mainClass in assembly := Some("nettunit.DemoApp")

resourceDirectory in Compile := file(".") / "./src/main/resources"
resourceDirectory in Runtime := file(".") / "./src/main/resources"


// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true