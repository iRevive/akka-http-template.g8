package $organization$.persistence.mongo.bson

import java.time._

import cats.syntax.either.{catsSyntaxEither, catsSyntaxEitherObject}
import $organization$.persistence.mongo.MongoError
import $organization$.persistence.mongo.MongoError.BsonDecodingError
import eu.timepit.refined.api.{RefType, Validate}
import magnolia._
import org.bson.BsonSerializationException
import org.mongodb.scala.bson._
import simulacrum.typeclass

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.language.implicitConversions

@implicitNotFound(
  """
 No BsonDecoder found for type \${A}. Try to implement an implicit BsonDecoder[\${A}].
 You can implement it in \${A} companion class.
    """
)
@typeclass
trait BsonDecoder[A] {

  def decode(value: BsonValue): Either[BsonDecodingError, A]

}

trait UnsafeBsonDecoder[A] extends BsonDecoder[A] {

  override final def decode(value: BsonValue): Either[BsonDecodingError, A] = {
    Either.catchNonFatal(unsafeReader(value)).leftMap(BsonDecodingError.apply)
  }

  protected def unsafeReader(value: BsonValue): A

}

object BsonDecoder extends BsonDecoderInstances {

  def stringDecoder[A](op: String => A): BsonDecoder[A] = new UnsafeBsonDecoder[A] {
    override protected def unsafeReader(value: BsonValue): A = op(value.asString().getValue)
  }

  def decodeOption[A: BsonDecoder](value: Option[BsonValue]): Either[BsonDecodingError, Option[A]] = {
    value match {
      case Some(v) => BsonDecoder[A].decode(v).map(Option.apply)
      case None    => Right(None)
    }
  }

  def decodeList[A: BsonDecoder](value: Seq[BsonValue]): Either[BsonDecodingError, List[A]] = {
    import cats.instances.either.catsStdInstancesForEither
    import cats.instances.list.catsStdInstancesForList
    import cats.syntax.traverse.toTraverseOps

    value.map(BsonDecoder[A].decode).toList.sequence[BsonDecodingErrorOr, A]
  }

  implicit class RichDocument(private val doc: BsonDocument) extends AnyVal {

    def as[A: BsonDecoder]: Either[BsonDecodingError, A] = BsonDecoder[A].decode(doc)

    def as[A: BsonDecoder](name: String): Either[BsonDecodingError, A] = BsonDecoder[A].decode(doc.get(name))

  }

}

trait BsonDecoderInstances {

  protected type BsonDecodingErrorOr[R] = Either[BsonDecodingError, R]

  implicit object StringBsonDecoder extends UnsafeBsonDecoder[String] {
    override protected def unsafeReader(value: BsonValue): String =
      value.asString().getValue
  }

  implicit object IntBsonDecoder extends UnsafeBsonDecoder[Int] {
    override protected def unsafeReader(value: BsonValue): Int =
      value.asInt32().intValue()
  }

  implicit object LongBsonDecoder extends UnsafeBsonDecoder[Long] {
    override protected def unsafeReader(value: BsonValue): Long =
      value.asInt64().longValue()
  }

  implicit object BigDecimalBsonDecoder extends UnsafeBsonDecoder[BigDecimal] {
    override protected def unsafeReader(value: BsonValue): BigDecimal =
      value.asDecimal128().decimal128Value().bigDecimalValue()
  }

  implicit object BooleanBsonDecoder extends UnsafeBsonDecoder[Boolean] {
    override protected def unsafeReader(value: BsonValue): Boolean =
      value.asBoolean().getValue
  }

  implicit object BinaryBsonDecoder extends UnsafeBsonDecoder[Array[Byte]] {
    override protected def unsafeReader(value: BsonValue): Array[Byte] =
      value.asBinary().getData
  }

  implicit object DoubleBsonDecoder extends UnsafeBsonDecoder[Double] {
    override protected def unsafeReader(value: BsonValue): Double =
      value.asDouble().doubleValue()
  }

  implicit object ZonedDateTimeBsonDecoder extends UnsafeBsonDecoder[ZonedDateTime] {
    override protected def unsafeReader(value: BsonValue): ZonedDateTime =
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.asDateTime().getValue), ZoneOffset.UTC)
  }

  implicit object ObjectIdDecoder extends UnsafeBsonDecoder[ObjectId] {
    override protected def unsafeReader(value: BsonValue): ObjectId = {
      value match {
        case id: BsonObjectId => id.getValue
        case other            => throw new BsonSerializationException(s"The value \$other is not ObjectId")
      }
    }
  }

  implicit object BsonDocumentDecoder extends UnsafeBsonDecoder[BsonDocument] {
    override protected def unsafeReader(value: BsonValue): BsonDocument = {
      value match {
        case v: BsonDocument => v
        case other           => throw new BsonSerializationException(s"The value \$other is not BsonDocument")
      }
    }
  }

  implicit def mapBsonDecoder[A, B: BsonDecoder](implicit view: String => A): BsonDecoder[Map[A, B]] = value => {
    Option(value) match {
      case None =>
        Right(Map.empty)

      case Some(document: BsonDocument) =>
        import scala.collection.JavaConverters._

        val zero = Either.right[BsonDecodingError, Map[A, B]](Map.empty)

        document.asScala.mapValues(BsonDecoder[B].decode).foldLeft(zero) {
          case (Right(_), (_, Left(error))) =>
            Left(error)

          case (Right(acc), (key, Right(v))) =>
            Right(acc + ((view(key), v)))

          case (other, _) =>
            other
        }

      case other =>
        Left(
          MongoError.BsonDecodingError(
            new BsonSerializationException(s"The value \$other is not a document and can't treat as document as well")
          )
        )
    }
  }

  implicit final def listBsonDecoder[A: BsonDecoder]: BsonDecoder[List[A]] = (value: BsonValue) => {
    Option(value) match {
      case None =>
        Right(Nil)

      case Some(array: BsonArray) =>
        import cats.instances.either.catsStdInstancesForEither
        import cats.instances.list.catsStdInstancesForList
        import cats.syntax.traverse.toTraverseOps

        import scala.collection.JavaConverters._

        array.getValues.asScala.map(BsonDecoder[A].decode).toList.sequence[BsonDecodingErrorOr, A]

      case other =>
        Left(
          MongoError.BsonDecodingError(
            new BsonSerializationException(s"The value \$other is not an array and can't treat as array as well")
          )
        )
    }
  }

  implicit final def optionBsonDecoder[A: BsonDecoder]: BsonDecoder[Option[A]] = (value: BsonValue) => {
    Option(value) match {
      case Some(v) if v.isNull =>
        Right(None)

      case Some(v) =>
        BsonDecoder[A].decode(v).map(Option.apply)

      case None =>
        Right(None)
    }
  }

  implicit final def refinedDecoder[T, P, F[_, _]](implicit underlying: BsonDecoder[T],
                                                   validate: Validate[T, P],
                                                   refType: RefType[F]): BsonDecoder[F[T, P]] = { value: BsonValue =>
    underlying.decode(value) match {
      case Right(t0) =>
        refType.refine(t0) match {
          case Left(err) => Left(MongoError.BsonDecodingError(new IllegalArgumentException(err)))
          case Right(t)  => Right(t)
        }
      case Left(e) => Left(e)
    }
  }

}

object BsonDecoderDerivation {

  type Typeclass[T] = BsonDecoder[T]

  def combine[T](ctx: CaseClass[Typeclass, T]): BsonDecoder[T] = new UnsafeBsonDecoder[T]() {
    override protected def unsafeReader(value: BsonValue): T = {
      ctx.construct { param =>
        param.typeclass.decode(value.asDocument().get(param.label)) match {
          case Left(error) => throw new IllegalArgumentException(error.message)
          case Right(v)    => v
        }
      }
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = new UnsafeBsonDecoder[T]() {
    override protected def unsafeReader(value: BsonValue): T = {
      throw new IllegalArgumentException(s"Decoding of trait [\${ctx.typeName}] is not supported [\$value]")
    }
  }

  implicit def derive[T]: Typeclass[T] = macro Magnolia.gen[T]

}
