package $organization$.persistence.mongo

import $organization$.it.ITSpec
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.{equal => eQual}
import scala.util.Random

class MongoRepositorySpec extends ITSpec {

  "MongoRepository" should {

    "persist and retrieve a value" in {
      val collectionName = randomNonEmptyString()
      val repository = new MongoRepository[BsonDocument](persistenceModule.mongoDatabase, collectionName)

      val name = randomString()
      val number = Random.nextInt()

      val document = BsonDocument("name" -> name, "number" -> number)

      repository.insertOne(document).runSyncUnsafe() shouldBe right

      val result = repository.findOne(eQual("name", name)).runSyncUnsafe().value.value

      result shouldBe document
    }

  }

}