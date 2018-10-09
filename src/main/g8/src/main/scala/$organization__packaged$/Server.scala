package $organization$

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import $organization$.util.ResultT.{defer, deferFuture}
import $organization$.util.{BaseError, TimeUtils}
import $organization$.util.logging.Loggable.InterpolatorOps._
import $organization$.util.logging.{Logging, TraceId}
import monix.eval.Task
import monix.execution.Scheduler

object Server extends Logging {

  def main(args: Array[String]): Unit = {
    startApp(ApplicationLoader.Default)
  }

  private def startApp(applicationLoader: ApplicationLoader): Unit = {
    implicit val traceId: TraceId = TraceId(log"Startup-\${TimeUtils.zonedDateTimeNow()}")
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: Materializer = ActorMaterializer()
    implicit val scheduler: Scheduler = monix.execution.Scheduler.Implicits.global

    val onCancel: Task[Unit] = {
      for {
        _ <- Task(logger.error("Application initialization was cancelled"))
        _ <- Task.fromFuture(system.terminate())
      } yield ()
    }

    def onSuccess(result: Either[BaseError, Unit]): Task[Unit] = result match {
      case Right(()) =>
        for {
          _ <- Task(logger.info("Application stopped without any error"))
          _ <- Task.fromFuture(system.terminate())
        } yield ()

      case Left(error) =>
        for {
          _ <- Task(logger.error(log"Error during application initialization. \$error"))
          _ <- Task.fromFuture(system.terminate())
        } yield ()
    }

    def onError(unhandledError: Throwable): Task[Unit] = {
      for {
        _ <- Task(logger.error(log"Error during application initialization. \$unhandledError", unhandledError))
        _ <- Task.fromFuture(system.terminate())
      } yield ()
    }

    val initializationFlow = for {
      _ <- defer(logger.info("Starting the \$name\$ service"))

      modules <- applicationLoader.loadModules()

      apiConfig = modules.apiModule.config

      _ = logger.info(log"Application trying to bind to host [\${apiConfig.host}:\${apiConfig.port}]")

      _ <- deferFuture(Http(system).bindAndHandle(modules.apiModule.routes, apiConfig.host.value, apiConfig.port.value))

      _ = logger.info(log"Application bound to [\${apiConfig.host}:\${apiConfig.port}]")
    } yield ()

    initializationFlow
      .value
      .doOnCancel(onCancel)
      .transformWith(onSuccess, onError)
      .runAsync

    ()
  }

}