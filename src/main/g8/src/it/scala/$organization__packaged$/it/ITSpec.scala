package $organization$.it

import cats.scalatest.{EitherMatchers, EitherValues}
import $organization$.ApplicationLoader
import $organization$.persistence.PersistenceModule
import $organization$.util.{BaseError, ClassUtils, ResultT}
import $organization$.util.ResultT.deferTask
import $organization$.util.logging.{Logging, TraceId}
import eu.timepit.refined.types.string.NonEmptyString
import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest._

import scala.concurrent.duration._

trait ITSpec extends WordSpecLike with Matchers
  with EitherValues
  with OptionValues
  with EitherMatchers
  with Inside
  with Logging {

  protected implicit val traceId: TraceId = TraceId(ClassUtils.getClassSimpleName(getClass))

  protected implicit val DefaultScheduler: Scheduler = ITSpec.DefaultScheduler

  protected def DefaultTimeout: FiniteDuration = ITSpec.DefaultTimeout

  protected def applicationLoader: ApplicationLoader = ITSpec.applicationLoader

  protected def applicationModules: ApplicationLoader.ApplicationModules = ITSpec.applicationModules.value

  protected def persistenceModule: PersistenceModule = applicationModules.persistenceModule

  protected def randomString(): String = scala.util.Random.alphanumeric.take(10).map(_.toLower).mkString

  protected def randomNonEmptyString(): NonEmptyString = NonEmptyString.unsafeFrom(randomString())

  protected implicit class RichResultT[A](private val underlying: ResultT[A]) {

    def runSyncUnsafe(timeout: Duration = DefaultTimeout): Either[BaseError, A] = {
      underlying.value.runSyncUnsafe(timeout)
    }

    def sleep(duration: FiniteDuration): ResultT[A] = {
      for {
        r <- underlying
        _ <- deferTask(Task.sleep(duration))
      } yield r
    }

  }

}

object ITSpec extends Logging {

  private implicit val DefaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  private val DefaultTimeout = 10.seconds

  private lazy val applicationLoader: ApplicationLoader = ApplicationLoader.Default

  private lazy val applicationModules = {
    implicit val traceId: TraceId = TraceId("ITSpec")

    applicationLoader.loadModules().value.runSyncUnsafe(40.seconds)
  }

}