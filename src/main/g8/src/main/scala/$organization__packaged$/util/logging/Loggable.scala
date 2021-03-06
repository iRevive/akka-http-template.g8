package $organization$.util
package logging

import java.time.ZonedDateTime
import java.util.UUID

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}
import cats.data.NonEmptyList
$if(useMongo.truthy)$import com.mongodb.DBObject$endif$
import eu.timepit.refined.api.RefType
import magnolia._
import simulacrum.typeclass

import scala.annotation.implicitNotFound
import scala.concurrent.duration.FiniteDuration
import scala.language.experimental.macros
import scala.language.implicitConversions

@implicitNotFound(
  """
 No Loggable found for type \${A}. Try to implement an implicit Loggable[\${A}].
 You can implement it in \${A} companion class.
    """
)
@typeclass
trait Loggable[A] {

  def show(value: A): String

}

object Loggable extends LoggableInstances {

  def instance[A](op: A => String): Loggable[A] = (value: A) => op(value)

  def fromToString[A]: Loggable[A] =
    Loggable.instance(_.toString)

  final case class Shown(override val toString: String) extends AnyVal

  object Shown {
    implicit def mat[A](x: A)(implicit z: Loggable[A]): Shown = Shown(z show x)
  }

  trait InterpolatorOps {
    @inline
    implicit def toLoggableInterpolator(sc: StringContext): LoggableInterpolator = new LoggableInterpolator(sc)
  }

  object InterpolatorOps extends InterpolatorOps

  final class LoggableInterpolator(private val sc: StringContext) extends AnyVal {
    def log(args: Loggable.Shown*): String = sc.s(args: _*)
  }

}

trait LoggableInstances {

  implicit val stringLoggable: Loggable[String]   = Loggable.fromToString
  implicit val intLoggable: Loggable[Int]         = Loggable.fromToString
  implicit val shortLoggable: Loggable[Short]     = Loggable.fromToString
  implicit val longLoggable: Loggable[Long]       = Loggable.fromToString
  implicit val doubleLoggable: Loggable[Double]   = Loggable.fromToString
  implicit val floatLoggable: Loggable[Float]     = Loggable.fromToString
  implicit val booleanLoggable: Loggable[Boolean] = Loggable.fromToString
  implicit val uuidLoggable: Loggable[UUID]       = Loggable.fromToString

  implicit val positionLoggable: Loggable[Position] = Loggable.instance(v => s"Position(\${v.fullPosition})")

  $if(useMongo.truthy)$
  implicit val dbObjectLoggable: Loggable[DBObject] = Loggable.fromToString
  $endif$

  implicit def enumLoggable[E <: Enum[E]]: Loggable[E] = Loggable.instance(_.name())

  implicit def enumerationLoggable[E <: Enumeration]: Loggable[E#Value] = Loggable.fromToString

  implicit val zonedDateTimeLoggable: Loggable[ZonedDateTime] = Loggable.fromToString

  implicit val finiteDurationLoggable: Loggable[FiniteDuration] = Loggable.fromToString

  implicit val circeJsonLoggable: Loggable[io.circe.Json] = Loggable.instance(_.noSpaces)

  implicit val akkaHttpRequestLoggable: Loggable[HttpRequest]   = Loggable.fromToString
  implicit val akkaHttpResponseLoggable: Loggable[HttpResponse] = Loggable.fromToString
  implicit val akkaHttpStatusCodeLoggable: Loggable[StatusCode] = Loggable.fromToString

  implicit val throwableLoggable: Loggable[Throwable] =
    Loggable instance { throwable =>
      val className = ClassUtils.getClassSimpleName(throwable.getClass)
      val message   = Option(throwable.getMessage).getOrElse("<empty message>")

      s"\$className(\$message)"
    }

  implicit def listLoggable[A: Loggable]: Loggable[List[A]] = traversableLoggable

  implicit def seqLoggable[A: Loggable]: Loggable[Seq[A]] = traversableLoggable

  implicit def setLoggable[A: Loggable]: Loggable[Set[A]] = traversableLoggable

  implicit def nelLoggable[A: Loggable]: Loggable[NonEmptyList[A]] =
    Loggable.instance { value =>
      traversableLoggable[A, List].show(value.toList)
    }

  implicit def mapLoggable[A: Loggable, B: Loggable]: Loggable[Map[A, B]] =
    Loggable instance { value =>
      traversableLoggable[(A, B), List].show(value.toList)
    }

  implicit def optionLoggable[A: Loggable]: Loggable[Option[A]] =
    Loggable instance { value =>
      value.fold("None")(Loggable[A].show)
    }

  implicit def tuple2[A, B](implicit a: Loggable[A], b: Loggable[B]): Loggable[(A, B)] =
    Loggable instance {
      case (first, second) =>
        s"(\${a.show(first)}, \${b.show(second)})"
    }

  implicit def eitherLoggable[A, B](implicit left: Loggable[A], right: Loggable[B]): Loggable[Either[A, B]] =
    Loggable instance {
      case Left(value)  => s"Left(\${left.show(value)})"
      case Right(value) => s"Right(\${right.show(value)})"
    }

  implicit def refinedLoggable[T, P, F[_, _]](implicit underlying: Loggable[T], refType: RefType[F]): Loggable[F[T, P]] =
    Loggable instance { value =>
      underlying.show(refType.unwrap(value))
    }

  def traversableLoggable[A, M[X] <: TraversableOnce[X]](implicit ev: Loggable[A]): Loggable[M[A]] =
    Loggable.instance(value => value.map(ev.show).mkString("[", ", ", "]"))

}

object LoggableDerivation {

  type Typeclass[T] = Loggable[T]

  def combine[T](ctx: CaseClass[Typeclass, T]): Loggable[T] = Loggable.instance { value =>
    if (ctx.isValueClass) {
      val param = ctx.parameters.head
      param.typeclass.show(param.dereference(value))
    } else {
      val paramStrings = ctx.parameters.flatMap { param =>
        param.dereference(value) match {
          case v: Option[_] if v.isEmpty =>
            None

          case v: Seq[_] if v.isEmpty =>
            None

          case other =>
            Some(s"\${param.label} = \${param.typeclass.show(other)}")
        }
      }

      s"\${ctx.typeName.short}(\${paramStrings.mkString(", ")})"
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = Loggable.instance { value =>
    ctx.dispatch(value) { sub =>
      sub.typeclass.show(sub.cast(value))
    }
  }

  implicit def derive[T]: Typeclass[T] = macro Magnolia.gen[T]

}
