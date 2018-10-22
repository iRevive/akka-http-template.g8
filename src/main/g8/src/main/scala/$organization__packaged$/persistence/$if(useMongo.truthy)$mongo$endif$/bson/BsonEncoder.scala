package $organization$.persistence.mongo.bson

import java.time._

import $organization$.util.TimeUtils
import eu.timepit.refined.api.RefType
import magnolia.{CaseClass, Magnolia, SealedTrait}
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.collection.immutable.{Document => ImmutableDocument}
import simulacrum.typeclass

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.language.implicitConversions

@implicitNotFound(
  """
 No BsonEncoder found for type \${A}. Try to implement an implicit BsonEncoder[\${A}].
 You can implement it in \${A} companion class.
    """
)
@typeclass
trait BsonEncoder[A] {

  def encode(value: A): BsonValue

}

object BsonEncoder extends BsonEncoderInstances {

  def stringEncoder[A](op: A => String): BsonEncoder[A] = (value: A) => BsonString(op(value))

}

trait BsonEncoderInstances {

  implicit val stringBsonEncoder       : BsonEncoder[String]            = v => BsonString(v)
  implicit val intBsonEncoder          : BsonEncoder[Int]               = v => BsonNumber(v)
  implicit val longBsonEncoder         : BsonEncoder[Long]              = v => BsonNumber(v)
  implicit val doubleBsonEncoder       : BsonEncoder[Double]            = v => BsonNumber(v)
  implicit val bigDecimalBsonEncoder   : BsonEncoder[BigDecimal]        = v => BsonDecimal128(v)
  implicit val binaryBsonEncoder       : BsonEncoder[Array[Byte]]       = v => BsonBinary(v)
  implicit val booleanBsonEncoder      : BsonEncoder[Boolean]           = v => BsonBoolean(v)
  implicit val objectIdBsonEncoder     : BsonEncoder[ObjectId]          = v => BsonObjectId(v)
  implicit val bsonDocumentEncoder     : BsonEncoder[BsonDocument]      = v => v
  implicit val immutableDocumentEncoder: BsonEncoder[ImmutableDocument] = v => v.toBsonDocument

  implicit val zonedDateTimeBsonEncoder: BsonEncoder[ZonedDateTime] = v => {
    BsonDateTime(v.withZoneSameLocal(TimeUtils.DefaultZone).toInstant.toEpochMilli)
  }

  implicit object MapBsonEncoder extends BsonEncoder[Map[String, String]] {
    override def encode(value: Map[String, String]): BsonValue = BsonDocument(
      value.mapValues(BsonString.apply)
    )
  }

  implicit def mapBsonEncoder[A, B: BsonEncoder](implicit view: A => String): BsonEncoder[Map[A, B]] =
    (value: Map[A, B]) => {
      BsonDocument(
        value map { case (key, v) => (view(key), BsonEncoder[B].encode(v)) }
      )
    }

  implicit final def optionEncoder[A: BsonEncoder]: BsonEncoder[Option[A]] =
    (value: Option[A]) => {
      value.fold[BsonValue](BsonNull())(BsonEncoder[A].encode)
    }

  implicit final def listBsonEncoder[A: BsonEncoder]: BsonEncoder[List[A]] =
    (value: List[A]) => {
      BsonArray(value.map(BsonEncoder[A].encode))
    }

  implicit final def refinedEncoder[T, P, F[_, _]](implicit
                                                   underlying: BsonEncoder[T],
                                                   refType: RefType[F]): BsonEncoder[F[T, P]] = { value: F[T, P] =>
    underlying.encode(refType.unwrap(value))
  }

}

object BsonEncoderDerivation {

  type Typeclass[T] = BsonEncoder[T]

  def combine[T](ctx: CaseClass[Typeclass, T]): BsonEncoder[T] = { value: T =>
    if (ctx.isValueClass) {
      throw new IllegalArgumentException("Value class cannot be encoded via derived instance")
    } else {
      val args = ctx
        .parameters
        .map { param => param.label -> param.typeclass.encode(param.dereference(value)) }

      BsonDocument(args)
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = { value: T =>
    ctx.dispatch(value) { subtype =>
      throw new IllegalArgumentException(s"Encoding of trait is not supported [\$value]. Subtype [\$subtype]")
    }
  }

  implicit def derive[T]: Typeclass[T] = macro Magnolia.gen[T]

}