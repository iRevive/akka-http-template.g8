package $organization$.util

import $organization$.util.logging.TraceId
import eu.timepit.refined.auto._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.TimeoutException
import scala.concurrent.duration._

class TaskUtilsSpec extends WordSpec with Matchers {

  private implicit val traceId: TraceId = TraceId()

  "TaskUtils" when {

    "retry" should {

      "execute task only once in case of no error" in {
        var inc = 0

        val executionResult = "test func"

        val task = Task {
          inc = inc + 1
          executionResult
        }

        val timeoutError = Task.raiseError[String](
          new TimeoutException("Timeout exception")
        )

        val retryPolicy = RetryPolicy(5, 10.millis, 100.millis)

        val result = TaskUtils.retry("retry spec", task, retryPolicy, timeoutError).runSyncUnsafe(500.millis)

        result shouldBe executionResult
        inc shouldBe 1
      }

      "re-execute task required amount of retries" in {
        var inc = 0

        val exception = new Exception("It's not working")

        val task = Task {
          inc = inc + 1
          throw exception
        }

        val timeoutError = Task.raiseError[Unit](
          new TimeoutException("Timeout exception")
        )

        val retryPolicy = RetryPolicy(5, 10.millis, 100.millis)

        val error = intercept[Exception](TaskUtils.retry("retry spec", task, retryPolicy, timeoutError).runSyncUnsafe(500.millis))

        error shouldBe exception
        inc shouldBe 6
      }

      "use a timeout task as a fallback in case of timeout" in {
        var inc = 0

        val exception = new Exception("It's not working")

        val timeoutException = new TimeoutException("Timeout exception")

        val task = Task {
          inc = inc + 1
          throw exception
        }

        val timeoutError = Task.raiseError[Unit](timeoutException)

        val retryPolicy = RetryPolicy(5, 30.millis, 40.millis)

        val error = intercept[Exception](TaskUtils.retry("retry spec", task, retryPolicy, timeoutError).runSyncUnsafe(500.millis))

        error shouldBe timeoutException
        inc shouldBe 2
      }

    }

  }

}
