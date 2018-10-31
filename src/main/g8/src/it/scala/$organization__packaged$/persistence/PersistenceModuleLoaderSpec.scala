package $organization$.persistence

import com.typesafe.config.ConfigFactory
$if(useMongo.truthy)$
import $organization$.persistence.mongo.MongoConfig
import $organization$.persistence.mongo.MongoError.UnhandledMongoError
import $organization$.it.ITSpec
import $organization$.util.config.ConfigParsingError
import monix.eval.Task
import org.mongodb.scala.MongoDatabase
$else$
import $organization$.test.BaseSpec
$endif$


class PersistenceModuleLoaderSpec extends ITSpec {

  "PersistenceModuleLoader" when {

    $if(useMongo.truthy)$
    "loading Mongo module" should {

      "return an error" when {

        "config is missing" in {
          val config = ConfigFactory.parseString(
            """
              |application.persistence {
              |
              |}
            """.stripMargin
          )

          val error = new PersistenceModuleLoader(config).loadMongoDatabase().runSyncUnsafe().leftValue

          inside(error) {
            case ConfigParsingError(path, expectedClass, err) =>
              path shouldBe "application.persistence.mongodb"
              expectedClass shouldBe "MongoConfig"
              err.getMessage shouldBe "Path not found in config"
          }
        }

        "config is invalid" in {
          val config = ConfigFactory.parseString(
            """
              |application.persistence.mongodb {
              |  uri = "mongodb://localhost:27017/?streamType=netty"
              |  database = "$name_normalized$"
              |  connection-attempt-timeout = 500 milliseconds
              |  retry-policy {
              |    retries = 10
              |    timeout = 60 seconds
              |  }
              |}
            """.stripMargin
          )

          val error = new PersistenceModuleLoader(config).loadMongoDatabase().runSyncUnsafe().leftValue

          inside(error) {
            case ConfigParsingError(path, expectedClass, err) =>
              path shouldBe "application.persistence.mongodb"
              expectedClass shouldBe "MongoConfig"
              err.getMessage shouldBe "Attempt to decode value on failed cursor: DownField(delay),DownField(retry-policy)"
          }
        }

        "connection in unreachable" in {
          val config = ConfigFactory.parseString(
            """
              |application.persistence.mongodb {
              |  uri = "mongodb://localhost:27017/?streamType=netty"
              |  database = "$name_normalized$"
              |  connection-attempt-timeout = 5 milliseconds
              |  retry-policy {
              |    retries = 5
              |    delay = 30 milliseconds
              |    timeout = 1000 milliseconds
              |  }
              |}
            """.stripMargin
          )

          var counter = 0

          val loader = new PersistenceModuleLoader(config) {
            override private[persistence] def mongoConnectionAttempt(db: MongoDatabase, config: MongoConfig): Task[Unit] = {
              for {
                _ <- Task.eval { counter = counter + 1 }
                _ <- super.mongoConnectionAttempt(db, config)
              } yield ()
            }
          }

          val error = loader.loadMongoDatabase().runSyncUnsafe().leftValue

          inside(error) {
            case UnhandledMongoError(cause) =>
              cause.getMessage shouldBe "Cannot acquire MongoDB connection in [1 second]"
          }

          counter shouldBe 6
        }

      }

    }
    $endif$

  }

}
