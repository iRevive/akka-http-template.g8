package $organization$.persistence.mongo

import $organization$.util.{BaseError, Position, ThrowableError}

sealed trait MongoError extends BaseError

object MongoError {

  case class UnhandledMongoError(cause: Throwable)(implicit val pos: Position) extends MongoError with ThrowableError

  case class BsonDecodingError(cause: Throwable)(implicit val pos: Position) extends MongoError with ThrowableError

}