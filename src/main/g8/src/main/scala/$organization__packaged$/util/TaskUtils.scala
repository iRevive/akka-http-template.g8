package $organization$.util

import $organization$.util.logging.{Logging, TraceId}
import $organization$.util.logging.Loggable.InterpolatorOps._
import eu.timepit.refined.types.numeric.NonNegInt
import monix.eval.Task

object TaskUtils extends Logging {

  def retry[A](name: String, task: Task[A], retryPolicy: RetryPolicy, onTimeout: Task[A])
              (implicit traceId: TraceId): Task[A] = {
    task
      .onErrorRecoverWith {
        case error if retryPolicy.retries.value > 0 =>
          logger.error(
            log"[\$name] Retry policy. " +
              log"Current retires [\${retryPolicy.retries}]. " +
              log"Delay [\${retryPolicy.delay}]. " +
              log"Timeout [\${retryPolicy.timeout}]. " +
              log"Error \$error"
          )

          retry(name, task, retryPolicy.copy(retries = NonNegInt.unsafeFrom(retryPolicy.retries.value - 1)), onTimeout)
            .delayExecution(retryPolicy.delay)

        case error =>
          Task.raiseError(error)
      }
      .timeoutTo(retryPolicy.timeout, onTimeout)
  }

}