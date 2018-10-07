package $organization$.persistence.mongo

$if(useMongo.truthy)$
import $organization$.util.RetryPolicy
import $organization$.util.logging.{Loggable, LoggableDerivation}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uri
import eu.timepit.refined.types.string.NonEmptyString

case class MongoConfig(url: String Refined Uri,
                       database: NonEmptyString,
                       retryPolicy: RetryPolicy)

object MongoConfig {

  import io.circe.Decoder
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.auto._
  import io.circe.refined._

  implicit val configuration: Configuration = Configuration.default.withKebabCaseMemberNames
  implicit val decoder: Decoder[MongoConfig] = exportDecoder[MongoConfig].instance
  implicit val loggableInstance: Loggable[MongoConfig] = LoggableDerivation.derive

}
$endif$