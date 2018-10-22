package $organization$.util

import cats.syntax.either.catsSyntaxEither
import $organization$.test.BaseSpec
import $organization$.util.ResultT._
import $organization$.util.logging.Loggable
import org.scalatest.Assertion

import scala.concurrent.Future

class ResultTOpsSpec extends BaseSpec {

  "ResultT" should {

    "#unit" in {
      unit().runSyncUnsafe() shouldBe right
    }

    "#leftT" in new resultMatcher[BaseError, String] {

      override lazy val delayedValue: BaseError = ThrowableError(new RuntimeException("my error"))

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Left(delayedValue)

      override def method(v: => BaseError): ResultT[String] = leftT(v)

    }

    "#eval" in new resultMatcher[String, String] {

      override def delayedValue: String = "213"

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Right(delayedValue)

      override def method(v: => String): ResultT[String] = eval(v)

    }

    "#evalEither(Left)" in new resultMatcher[BaseError, String] {

      override lazy val delayedValue: BaseError = ThrowableError(new RuntimeException("my error"))

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Left(delayedValue)

      override def method(v: => BaseError): ResultT[String] = evalEither(Left(v))

    }

    "#evalEither(Right)" in new resultMatcher[String, String] {

      override def delayedValue: String = "213"

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Right(delayedValue)

      override def method(v: => String): ResultT[String] = evalEither(Right(v))

    }

    "#safe" in new resultMatcher[String, String] {

      override def delayedValue: String = "213"

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Right(delayedValue)

      override def method(v: => String): ResultT[String] = safe(v)

    }

    "#safe throw" in new resultMatcher[String, String] {

      private lazy val exception = new RuntimeException("my error")

      override def delayedValue: String = throw exception

      override def matcher(v: Either[BaseError, String]): Assertion =
        v.leftMap(_.message) shouldBe Left(Loggable[Throwable].show(exception))

      override def method(v: => String): ResultT[String] = safe(v)

    }

    "#deferFuture" in new resultMatcher[Future[String], String] {

      override def delayedValue: Future[String] = Future.successful("321")

      override def matcher(v: Either[BaseError, String]): Assertion = v shouldBe Right("321")

      override def method(v: => Future[String]): ResultT[String] = deferFuture(v)

    }

    "#deferFuture throw" in new resultMatcher[Future[String], String] {

      private lazy val exception = new RuntimeException("my error")

      override def delayedValue: Future[String] = throw exception

      override def matcher(v: Either[BaseError, String]): Assertion =
        v.leftMap(_.message) shouldBe Left(Loggable[Throwable].show(exception))

      override def method(v: => Future[String]): ResultT[String] = deferFuture(v)

    }

    "#deferFuture throw in flatMap" in new resultMatcher[Future[String], String] {

      private lazy val exception = new RuntimeException("my error")

      override def delayedValue: Future[String] = Future(1) flatMap { _ => throw exception }

      override def matcher(v: Either[BaseError, String]): Assertion =
        v.leftMap(_.message) shouldBe Left(Loggable[Throwable].show(exception))

      override def method(v: => Future[String]): ResultT[String] = deferFuture(v)

    }

  }

  private trait resultMatcher[A, B] {

    def delayedValue: A

    def method(v: => A): ResultT[B]

    def matcher(v: Either[BaseError, B]): Assertion

    var value: Int = 0

    lazy val resultValue: A = {
      value = value + 1
      delayedValue
    }

    value shouldBe 0

    val asyncResult: ResultT[B] = method(resultValue)

    value shouldBe 0

    matcher(asyncResult.runSyncUnsafe())

    value shouldBe 1
  }

}
