package $organization$.persistence

import com.typesafe.scalalogging.StrictLogging
$if(useMongo.truthy)$
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoDatabase}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal
$endif$


$if(useMongo.truthy)$
class PersistenceModule(mongoConfig: MongoConfig) extends StrictLogging {
$else$
class PersistenceModule() extends StrictLogging {
$endif$


  $if(useMongo.truthy)$
  lazy val mongoDatabase: MongoDatabase = {
    logger.info(s"Loading MongoDB module with config \$mongoConfig")

    val mongoClient: MongoClient = MongoClient(mongoConfig.url)

    val db = mongoClient.getDatabase(mongoConfig.database).withCodecRegistry(DEFAULT_CODEC_REGISTRY)

    try {
      Await.result(
        db.getCollection(mongoConfig.collection).find().limit(1).toFuture(),
        mongoConfig.testConnectionTimeout
      )

      logger.info("Successfully connected to the MongoDB")

      db
    } catch {
      case NonFatal(e) =>
        logger.error(s"Error received while connecting to MongoDB [\$e]")
        mongoClient.close()
        throw new RuntimeException(s"Error received while connecting to MongoDB [\$e]")
    }
  }
  $endif$

}
$if(useMongo.truthy)$

case class MongoConfig(url: String,
                       database: String,
                       testConnectionTimeout: FiniteDuration,
                       collection: String)

object MongoConfig {

  import io.circe.Decoder
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.auto._
  import io.circe.config.syntax.durationDecoder

  implicit val configuration: Configuration = Configuration.default.withKebabCaseMemberNames
  implicit val decoder: Decoder[MongoConfig] = exportDecoder[MongoConfig].instance

}
$endif$