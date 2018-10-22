package $organization$.persistence.mongo.bson

$if(useMongo.truthy)$
import $organization$.test.BaseSpec
import org.mongodb.scala.bson.{BsonDocument, BsonNull}

class BsonEncoderSpec extends BaseSpec {

  "BsonEncoder" should {

    "generate a typeclass" in {
      import BsonEncoderDerivation._

      case class B(property4: Long, property5: Option[String], property6: Option[Int])

      case class A(property1: String, property2: Int, property3: B)

      val instanceB = B(3L, None, Some(1))

      val instanceA = A("123", 2, instanceB)

      val result = BsonEncoder[A].encode(instanceA)

      val expected = BsonDocument(
        "property1" -> "123",
        "property2" -> 2,
        "property3" -> BsonDocument(
          "property4" -> 3L,
          "property5" -> BsonNull(),
          "property6" -> 1
        )
      )

      result shouldEqual expected
    }

  }

}
$endif$