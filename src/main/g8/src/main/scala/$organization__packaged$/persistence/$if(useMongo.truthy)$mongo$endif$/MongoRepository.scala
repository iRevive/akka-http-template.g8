package $organization$.persistence.mongo

import cats.data.EitherT
import $organization$.persistence.mongo.MongoError.UnhandledMongoError
import $organization$.persistence.mongo.bson.{BsonDecoder, BsonEncoder}
import $organization$.util.{BaseError, ResultT}
import $organization$.util.ResultT.{deferFuture, evalEither}
import eu.timepit.refined.types.string.NonEmptyString
import monix.eval.Task
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{FindOneAndReplaceOptions, ReturnDocument}
import org.mongodb.scala.{MongoCollection, MongoDatabase}

import scala.reflect.ClassTag
import scala.util.control.NonFatal

class MongoRepository[Obj: BsonEncoder: BsonDecoder](database: MongoDatabase, collectionName: NonEmptyString) {

  protected[mongo] val collection: ResultT[MongoCollection[BsonDocument]] = {
    getCollection[BsonDocument](collectionName)
  }

  final def findAndReplaceOne(criteria: Bson,
                              newValue: BsonDocument,
                              returnNew: Boolean,
                              upsert: Boolean): ResultT[Option[Obj]] = {
    for {
      c <- collection

      options = FindOneAndReplaceOptions()
        .upsert(upsert)
        .returnDocument(if (returnNew) ReturnDocument.AFTER else ReturnDocument.BEFORE)

      doc <- deferFuture(
        c.findOneAndReplace(filter = criteria, replacement = newValue, options = options).headOption(),
        UnhandledMongoError.apply
      )

      decoded <- evalEither(BsonDecoder.decodeOption[Obj](doc))
    } yield decoded
  }

  final def findOne(query: Bson): ResultT[Option[Obj]] = {
    for {
      c       <- collection
      result  <- deferFuture(c.find[BsonDocument](query).headOption(), UnhandledMongoError.apply)
      decoded <- evalEither(BsonDecoder.decodeOption[Obj](result))
    } yield decoded
  }

  final def find(query: Bson): ResultT[List[Obj]] = {
    for {
      c       <- collection
      result  <- deferFuture(c.find[BsonDocument](query).toFuture(), UnhandledMongoError.apply)
      decoded <- evalEither(BsonDecoder.decodeList[Obj](result))
    } yield decoded
  }

  final def insertOne(value: Obj): ResultT[Unit] = {
    for {
      c <- collection
      _ <- deferFuture(c.insertOne(BsonEncoder[Obj].encode(value).asDocument()).toFutureOption(), UnhandledMongoError.apply)
    } yield ()
  }

  protected[mongo] def getCollection[A: ClassTag](name: NonEmptyString): ResultT[MongoCollection[A]] = {
    EitherT {
      Task
        .evalOnce(database.getCollection[A](name.value))
        .map[Either[BaseError, MongoCollection[A]]](Right.apply)
        .onErrorRecover { case NonFatal(e) => Left(UnhandledMongoError(e)) }
    }
  }

}
