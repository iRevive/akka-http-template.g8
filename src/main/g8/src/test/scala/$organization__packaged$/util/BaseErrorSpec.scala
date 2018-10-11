package $organization$.util

import $organization$.test.BaseSpec

class BaseErrorSpec extends BaseSpec {
  import BaseErrorSpec._

  "BaseError" should {

    "use correct loggable instance" in {
      val message = randomString()
      val error = TestError(message)

      val expectedMessage = s"TestError(message = \$message, pos = $organization$.util.BaseErrorSpec#error:12)"

      error.toString shouldBe expectedMessage
    }

  }

  "ThrowableError" should {

    "show the real position of error creation" in {
      val exception = new RuntimeException("something went wrong")

      val error = ThrowableError(exception)

      val expectedMessage = "RuntimeException(something went wrong)"
      val expectedPosition = "$organization$.util.BaseErrorSpec#error:26"

      val expectedToString = "ThrowableError(" +
        s"message = \$expectedMessage, " +
        "cause = java.lang.RuntimeException: something went wrong, " +
        s"pos = \$expectedPosition)"

      error.message shouldBe expectedMessage
      error.pos.fullPosition shouldBe expectedPosition
      error.toString shouldBe expectedToString
    }

    "show the real class name of an error" in {
      val exception = new RuntimeException("something went wrong")

      val error = TestThrowableError(exception)

      val expectedMessage = "RuntimeException(something went wrong)"
      val expectedPosition = "$organization$.util.BaseErrorSpec#error:44"

      val expectedToString = "TestThrowableError(" +
        s"message = \$expectedMessage, " +
        "cause = java.lang.RuntimeException: something went wrong, " +
        s"pos = \$expectedPosition)"

      error.message shouldBe expectedMessage
      error.pos.fullPosition shouldBe expectedPosition
      error.toString shouldBe expectedToString
    }

    "unapply should return an exception" in {
      val exception = new RuntimeException("something went wrong")

      val error = ThrowableError(exception)

      inside(error) { case ThrowableError(throwable) =>
        throwable shouldBe exception
      }
    }

  }

}

object BaseErrorSpec {

  case class TestError(message: String)(implicit val pos: Position) extends BaseError

  case class TestThrowableError(cause: Throwable)(implicit val pos: Position) extends ThrowableError

}