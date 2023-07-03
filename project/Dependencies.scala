import sbt.*

object Dependencies {
  private val AkkaVersion = "2.8.2"
  private val AkkaHttpVersion = "10.5.0"
  private val CirceVersion = "0.14.3"

  val dependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "io.circe" %% "circe-core" % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser" % CirceVersion,
    "io.circe" %% "circe-generic-extras" % CirceVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

    "org.scalamock" %% "scalamock" % "5.2.0" % Test,
    "org.scalatest" %% "scalatest" % "3.2.11" % Test,
    "org.mockito" %% "mockito-scala" % "1.17.5" % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  )

}
