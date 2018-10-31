package $organization$.util
package logging

import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}

import scala.util.control.NonFatal

trait Logging {

  protected val logger: LoggerTakingImplicit[TraceId] =
    Logger.takingImplicit[TraceId](ClassUtils.getClassSimpleName(getClass))

  protected implicit final def toExtension(logger: LoggerTakingImplicit[TraceId]): Logging.Extension[TraceId] =
    new Logging.Extension[TraceId](logger)

}

object Logging {

  final class Extension[A] private[Logging] (val underlying: LoggerTakingImplicit[A]) extends AnyVal {

    def withError(message: String, error: BaseError)(implicit a: A): Unit = {
      error match {
        case ThrowableError(cause) =>
          underlying.error(message, cause)

        case NonFatal(cause) =>
          underlying.error(message, cause)

        case _ =>
          underlying.error(message)
      }
    }

  }

}
