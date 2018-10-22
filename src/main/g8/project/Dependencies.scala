import sbt._

object Settings {
  val organization          = "$organization$"
  val name                  = "$name_normalized$"
}

object Versions {
  val scala                 = "$scala_version$"

  val akka                  = "$akka_version$"
  val akkaHttp              = "$akka_http_version$"

  val circe                 = "$circe_version$"
  val circeConfig           = "$circe_config_version$"
  val refined               = "$refined_version$"

  val cats                  = "$cats_version$"
  val monix                 = "$monix_version$"

  $if(useMongo.truthy)$
  val mongoScalaDriver      = "$mongo_scala_driver_version$"
  val netty                 = "$netty_version$"
  $endif$

  val sourcecode            = "$sourcecode_version$"
  val simulacrum            = "$simulacrum_version$"
  val magnolia              = "$magnorlia_version$"

  val logback               = "$logback_version$"
  val scalaLogging          = "$scala_logging_version$"

  val scalatest             = "$scalatest_version$"
  val catsScalatest         = "$cats_scalatest_version$"
}

object Dependencies {

  val root = Seq(
    "com.typesafe.akka"           %% "akka-http"                % Versions.akkaHttp,
    "com.typesafe.akka"           %% "akka-actor"               % Versions.akka,
    "com.typesafe.akka"           %% "akka-stream"              % Versions.akka,

    "io.circe"                    %% "circe-core"               % Versions.circe,
    "io.circe"                    %% "circe-jawn"               % Versions.circe,
    "io.circe"                    %% "circe-generic"            % Versions.circe,
    "io.circe"                    %% "circe-generic-extras"     % Versions.circe,
    "io.circe"                    %% "circe-refined"            % Versions.circe,
    "io.circe"                    %% "circe-config"             % Versions.circeConfig,

    "eu.timepit"                  %% "refined"                  % Versions.refined,

    "org.typelevel"               %% "cats-core"                % Versions.cats,

    "io.monix"                    %% "monix"                    % Versions.monix,

    $if(useMongo.truthy)$
    "org.mongodb.scala"           %% "mongo-scala-driver"       % Versions.mongoScalaDriver,
    "io.netty"                    %  "netty-all"                % Versions.netty,
    $endif$

    "com.lihaoyi"                 %% "sourcecode"               % Versions.sourcecode,
    "com.github.mpilquist"        %% "simulacrum"               % Versions.simulacrum,
    "com.propensive"              %% "magnolia"                 % Versions.magnolia,

    "com.typesafe.scala-logging"  %% "scala-logging"            % Versions.scalaLogging,
    "ch.qos.logback"              %  "logback-classic"          % Versions.logback,

    "org.scalatest"               %% "scalatest"                % Versions.scalatest        % "it,test",
    "com.ironcorelabs"            %% "cats-scalatest"           % Versions.catsScalatest    % "it,test",
    "com.typesafe.akka"           %% "akka-http-testkit"        % Versions.akkaHttp         % "it,test"
  )

}