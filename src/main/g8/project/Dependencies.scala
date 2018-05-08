import sbt._

object Settings {
  val organization          = "$organization$"
  val name                  = "$name_normalized$"
}

object Versions {
  val scala                 = "$scala_version$"

  val akkaHttp              = "$akka_http_version$"
  val akkaHttpCirce         = "$akka_http_circe_version$"

  val circe                 = "$circe_version$"
  val circeConfig           = "$circe_config_version$"

  val cats                  = "$cats_version$"

  $if(useMongo.truthy)$
  val mongoScalaDriver      = "$mongo_scala_driver_version$"
  $endif$

  val logback               = "$logback_version$"
  val scalaLogging          = "$scala_logging_version$"

  val scalatest             = "$scalatest_version$"
  val scalamock             = "$scalamock_version$"
  val catsScalatest         = "$cats_scalatest_version$"
}

object Dependencies {

  val root = Seq(
    "com.typesafe.akka"           %% "akka-http"                % Versions.akkaHttp,
    "de.heikoseeberger"           %% "akka-http-circe"          % Versions.akkaHttpCirce,

    "io.circe"                    %% "circe-core"               % Versions.circe,
    "io.circe"                    %% "circe-jawn"               % Versions.circe,
    "io.circe"                    %% "circe-generic"            % Versions.circe,
    "io.circe"                    %% "circe-generic-extras"     % Versions.circe,
    "io.circe"                    %% "circe-config"             % Versions.circeConfig,

    "org.typelevel"               %% "cats-core"                % Versions.cats,

    $if(useMongo.truthy)$
    "org.mongodb.scala"           %% "mongo-scala-driver"       % Versions.mongoScalaDriver,
    $endif$

    "com.typesafe.scala-logging"  %% "scala-logging"            % Versions.scalaLogging,
    "ch.qos.logback"              %  "logback-classic"          % Versions.logback,

    $if(useMongo.truthy)$
    "org.scalatest"               %% "scalatest"                % Versions.scalatest        % "it,test",
    "org.scalamock"               %% "scalamock"                % Versions.scalamock        % "it,test",
    "com.ironcorelabs"            %% "cats-scalatest"           % Versions.catsScalatest    % "it,test",
    "com.typesafe.akka"           %% "akka-http-testkit"        % Versions.akkaHttp         % "it,test"
    $else$
    "org.scalatest"               %% "scalatest"                % Versions.scalatest        % "test",
    "org.scalamock"               %% "scalamock"                % Versions.scalamock        % "test",
    "com.ironcorelabs"            %% "cats-scalatest"           % Versions.catsScalatest    % "test",
    "com.typesafe.akka"           %% "akka-http-testkit"        % Versions.akkaHttp         % "test"
    $endif$
  )

}