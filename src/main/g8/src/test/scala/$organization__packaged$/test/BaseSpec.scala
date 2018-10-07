package $organization$.test

import java.util.concurrent.TimeUnit

import cats.scalatest.{EitherMatchers, EitherValues}
import $organization$.util.{BaseError, ResultT}
import $organization$.util.logging.TraceId
import eu.timepit.refined.types.string.NonEmptyString
import monix.execution.Scheduler
import org.scalatest._

import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec extends WordSpecLike
  with Matchers
  with EitherMatchers
  with EitherValues
  with OptionValues
  with Inside {

  protected implicit val traceId: TraceId = TraceId()

  protected implicit val DefaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  protected val DefaultTimeout = FiniteDuration(10, TimeUnit.SECONDS)

  protected def randomNonEmptyString(): NonEmptyString = NonEmptyString.unsafeFrom(randomString())

  protected def randomString(): String = scala.util.Random.alphanumeric.take(10).map(_.toLower).mkString

  protected implicit class RichEitherT[A](private val underlying: ResultT[A]) {

    def runSyncUnsafe(timeout: Duration = DefaultTimeout): Either[BaseError, A] = {
      underlying.value.runSyncUnsafe(timeout)
    }

  }

}
