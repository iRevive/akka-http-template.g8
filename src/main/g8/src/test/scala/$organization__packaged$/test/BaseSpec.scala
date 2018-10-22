package $organization$.test

import java.util.concurrent.TimeUnit

import cats.scalatest.{EitherMatchers, EitherValues}
import $organization$.util.{BaseError, ResultT}
import $organization$.util.logging.TraceId
import eu.timepit.refined.types.string.NonEmptyString
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.CanBlock
import org.scalatest._

import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec extends WordSpecLike with Matchers with EitherMatchers with EitherValues with OptionValues with Inside {

  protected implicit val traceId: TraceId = TraceId()

  protected implicit val DefaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  protected val DefaultTimeout = FiniteDuration(10, TimeUnit.SECONDS)

  protected def randomNonEmptyString(): NonEmptyString = NonEmptyString.unsafeFrom(randomString())

  protected def randomString(): String = scala.util.Random.alphanumeric.take(10).map(_.toLower).mkString

  protected implicit class RichResultT[A](private val underlying: ResultT[A]) {

    import cats.syntax.apply._

    def runSyncUnsafe(timeout: Duration = DefaultTimeout)(implicit s: Scheduler = DefaultScheduler): Either[BaseError, A] = {
      underlying.value.runSyncUnsafe(timeout)(s, implicitly[CanBlock])
    }

    def sleep(duration: FiniteDuration): ResultT[A] = {
      underlying <* ResultT.deferTask(Task.sleep(duration))
    }

  }

}
