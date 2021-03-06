package $organization$.util.logging

import $organization$.test.BaseSpec

class LoggableSpec extends BaseSpec {

  "Loggable" should {

    import LoggableSpec._

    "enrich an object when 'Loggable.ops._' is imported" in {
      import Loggable.ops._

      implicit val loggableInstance: Loggable[TestClass] = LoggableDerivation.derive[TestClass]

      val obj = TestClass(
        arg1 = Some("arg 1 string"),
        arg2 = None,
        arg3 = Some(10.21),
        arg4 = Nil,
        arg5 = List(321L)
      )

      val result = obj.show

      val expected = "TestClass(arg1 = arg 1 string, arg3 = 10.21, arg5 = [321])"

      result shouldBe expected
    }

    "enrich an object when 'ToLoggableOps' is mixed-in to the class" in new Loggable.ToLoggableOps {
      implicit val loggableInstance: Loggable[TestClass] = LoggableDerivation.derive[TestClass]

      val obj = TestClass(
        arg1 = Some("arg 1 string"),
        arg2 = None,
        arg3 = Some(10.21),
        arg4 = Nil,
        arg5 = List(321L)
      )

      val result = obj.show

      val expected = "TestClass(arg1 = arg 1 string, arg3 = 10.21, arg5 = [321])"

      result shouldBe expected
    }

  }

}

object LoggableSpec {

  case class TestClass(
      arg1: Option[String],
      arg2: Option[Double],
      arg3: Option[Double],
      arg4: List[Long],
      arg5: List[Long]
  )

  case class ValueClassTest(arg: String) extends AnyVal

}
