package $organization$.it

import java.util.concurrent.TimeUnit

import $organization$.util.ConfigOps
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{WordSpec, Matchers}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Awaitable}

trait BaseSpec extends WordSpec with Matchers with ConfigOps with StrictLogging {

  protected val DefaultTimeout = FiniteDuration(10, TimeUnit.SECONDS)

  protected def await[A](awaitable: Awaitable[A]): A = Await.result(awaitable, DefaultTimeout)

}