package $organization$.persistence.mongo.bson

$if(useMongo.truthy)$
import $organization$.test.BaseSpec
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonNull}

class BsonDecoderSpec extends BaseSpec {

  import BsonDecoder._

  "BsonDecoder" should {

    "correctly process missing and nullable values" in {
      val doc = BsonDocument("key" -> "value", "key2" -> BsonNull())

      doc.as[Option[String]]("key") should beRight(Option("value"))
      doc.as[Option[String]]("key2") should beRight(Option.empty[String])
      doc.as[Option[String]]("key3") should beRight(Option.empty[String])
    }

    "correctly process missing and nullable arrays" in {
      val doc = BsonDocument("key" -> BsonArray("value"), "key2" -> BsonArray())

      doc.as[List[String]]("key") should beRight(List("value"))
      doc.as[List[String]]("key2") should beRight(List.empty[String])
      doc.as[List[String]]("key3") should beRight(List.empty[String])
    }

    "generate a typeclass" in {
      import BsonDecoderDerivation._

      case class B(property4: Long, property5: Option[String], property6: Option[Int])

      case class A(property1: String, property2: Int, property3: B)

      val document = BsonDocument(
        "property1" -> "123",
        "property2" -> 2,
        "property3" -> BsonDocument(
          "property4" -> 3L,
          "property5" -> BsonNull(),
          "property6" -> 1
        )
      )

      val result = BsonDecoder[A].decode(document)

      val expected = A("123", 2, B(3L, None, Some(1)))

      result should beRight(expected)
    }

  }

}
$endif$