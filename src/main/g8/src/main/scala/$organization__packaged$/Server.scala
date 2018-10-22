package $organization$

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import cats.syntax.apply._
import $organization$.util.ResultT.{deferFuture, eval}
import $organization$.util.logging.Loggable.InterpolatorOps._
import $organization$.util.logging.{Logging, TraceId}
import $organization$.util.{BaseError, ThrowableError, TimeUtils}
import monix.eval.Task
import monix.execution.Scheduler

object Server extends Logging {

  // \$COVERAGE-OFF\$
  def main(args: Array[String]): Unit = {
    import scala.concurrent.duration._

    implicit val traceId: TraceId     = TraceId(log"Startup-\${TimeUtils.zonedDateTimeNow()}")
    implicit val system: ActorSystem  = ActorSystem()
    implicit val scheduler: Scheduler = monix.execution.Scheduler.Implicits.global

    startApp(ApplicationLoader.Default).runSyncUnsafe(100.seconds)
  }
  // \$COVERAGE-ON\$

  def startApp(applicationLoader: ApplicationLoader)(implicit traceId: TraceId,
                                                     system: ActorSystem,
                                                     scheduler: Scheduler): Task[Unit] = {
    implicit val mat: Materializer = ActorMaterializer()

    val terminator = Task.deferFuture(system.terminate()).map(_ => ())

    val onCancel: Task[Unit] = {
      Task(logger.error("Application initialization was cancelled")) *> terminator
    }

    def onSuccess(result: Either[BaseError, Unit]): Task[Unit] = result match {
      case Right(()) =>
        Task(logger.info("Application started successfully"))

      case Left(error: ThrowableError) =>
        Task(logger.error(log"Error during application initialization. \$error", error.cause)) *>
          terminator *>
          Task.raiseError(error.cause)

      case Left(error) =>
        Task(logger.error(log"Error during application initialization. \$error")) *>
          terminator *>
          Task.raiseError(new RuntimeException(error.message))
    }

    def onError(unhandledError: Throwable): Task[Unit] = {
      Task(logger.error(log"Error during application initialization. \$unhandledError", unhandledError)) *> terminator
    }

    val initializationFlow = for {
      _ <- eval(logger.info("Starting the $name$ service"))

      application <- applicationLoader.loadApplication()

      api = application.apiModule

      _ = logger.info(log"Application trying to bind to host [\${api.config.host}:\${api.config.port}]")

      _ <- deferFuture(Http(system).bindAndHandle(api.routes, api.config.host.value, api.config.port.value))

      _ = logger.info(log"Application bound to [\${api.config.host}:\${api.config.port}]")
    } yield ()

    initializationFlow.value
      .doOnCancel(onCancel)
      .transformWith(onSuccess, onError)
  }

}