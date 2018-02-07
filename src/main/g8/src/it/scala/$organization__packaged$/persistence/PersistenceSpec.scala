package $organization$.persistence

import $organization$.it.BaseSpec

class PersistenceSpec extends BaseSpec {

  $if(useMongo.truthy)$
  import org.mongodb.scala.bson.collection.immutable.Document
  import org.mongodb.scala.model.Filters.{equal => eQual}
  import scala.util.Random
  import scala.concurrent.ExecutionContext.Implicits.global

  "Mongo" should {

    "persist and retrieve a value" in {
      val name = Random.alphanumeric.take(10).mkString("")
      val number = Random.nextInt()

      val document = Document("name" -> name, "number" -> number)

      val collection = persistenceModule.mongoDatabase.getCollection(mongoConfig.collection)

      val resultAsync = for {
        _ <- collection.insertOne(document).toFuture()
        result <- collection.find(eQual("name", name)).headOption()
      } yield result

      val result = for {
        doc <- await(resultAsync)
        mutable = doc.toBsonDocument
        _ = mutable.remove("_id")
      } yield mutable

      result shouldEqual Some(document.toBsonDocument)
    }

  }

  private lazy val mongoConfig: MongoConfig = {
    import com.typesafe.config.ConfigFactory

    ConfigFactory.load().loadUnsafe[MongoConfig]("application.persistence.mongodb")
  }

  private lazy val persistenceModule: PersistenceModule = new PersistenceModule(mongoConfig)
  $endif$

}