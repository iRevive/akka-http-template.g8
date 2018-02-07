package $organization$

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import $organization$.api.Endpoints
import $organization$.persistence._
import $organization$.util.ConfigOps

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.control.NonFatal

object Server extends StrictLogging {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val system: ActorSystem = ActorSystem()

    try {
      val appLoader = Props(new AppLoader(config))
      system.actorOf(Props(new Terminator(appLoader)), "app-terminator")
    } catch {
      case NonFatal(e) =>
        logger.error(s"Application loading error [\${e.getMessage}]", e)
        system.terminate()
        throw e
    }
  }

}

final class AppLoader(config: Config) extends Actor with StrictLogging with ConfigOps {

  import io.circe.generic.auto._
  import io.circe.config.syntax._

  override def receive: Actor.Receive = Actor.emptyBehavior

  override def preStart(): Unit = startApp()

  private def startApp(): Unit = {
    implicit val ec: ExecutionContext = context.system.dispatcher
    implicit val mat: Materializer = ActorMaterializer()

    val endpoints = new Endpoints()

    val ServerSettings(host, port) = config.loadUnsafe[ServerSettings]("application.http")

    $if(useMongo.truthy)$
    val mongoConfig = config.loadUnsafe[MongoConfig]("application.persistence.mongodb")
    $endif$

    $if(useMongo.truthy)$
    val persistenceModule = new PersistenceModule(mongoConfig)
    $else$
    val persistenceModule = new PersistenceModule()
    $endif$

    logger.info(s"Application trying to bind to host [\$host:\$port]")

    Http(context.system).bindAndHandle(endpoints.routes, host, port)
      .map { _ => logger.info(s"Application bound to [\$host:\$port]") }
      .onComplete {
        case Failure(_) =>
          logger.info(s"Failed to bind to [\$host:\$port]")
          context.system.terminate()

        case _ =>
      }
  }

  case class ServerSettings(host: String, port: Int)

}

final class Terminator(appLoaderProps: Props) extends Actor with LazyLogging {

  context.actorOf(appLoaderProps, "app-loader")

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy() {
      case error =>
        logger.error(s"Application loading error [\${error.getMessage}]", error)
        context.system.terminate()
        Stop
    }
  }

  override def receive: Receive = Actor.emptyBehavior

}