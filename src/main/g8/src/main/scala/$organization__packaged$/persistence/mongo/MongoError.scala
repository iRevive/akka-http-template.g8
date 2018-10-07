package $organization$.persistence.mongo

$if(useMongo.truthy)$
import $organization$.util.{BaseError, Position, ThrowableError}

sealed trait MongoError extends BaseError

object MongoError {

  case class UnhandledMongoError(cause: Throwable)(implicit val pos: Position) extends MongoError with ThrowableError

  case class BsonDecodingError(cause: Throwable)(implicit val pos: Position) extends MongoError with ThrowableError

}
$endif$