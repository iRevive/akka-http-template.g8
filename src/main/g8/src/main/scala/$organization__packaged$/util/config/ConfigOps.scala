package $organization$.util
package config

import $organization$.util.logging.Loggable.InterpolatorOps._
import cats.syntax.either.catsSyntaxEither
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.{Decoder, Error}

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.control.NonFatal

trait ConfigOps {

  implicit def toRichConfig(config: Config): RichConfig = new RichConfig(config)

}

object ConfigOps extends ConfigOps

final class RichConfig(private val config: Config) extends AnyVal {
  import io.circe.config.syntax._

  def load[A: Decoder](path: String)(implicit ct: ClassTag[A]): Either[BaseError, A] = {
    config.as[A](path).leftMap(error => ConfigParsingError(path, ClassUtils.getClassSimpleName(ct.runtimeClass), error))
  }

  def loadMeta[A: Decoder](path: String)(implicit ct: ClassTag[A]): Either[BaseError, A] = {
    parseStringAsConfig(config.getString(path))
      .flatMap(_.as[A])
      .leftMap(error => ConfigParsingError(path, ClassUtils.getClassSimpleName(ct.runtimeClass), error))
  }

  private def parseStringAsConfig(input: => String): Either[io.circe.Error, Config] = {
    try {
      Right(ConfigFactory.parseString(input))
    } catch {
      case NonFatal(e) => Left(io.circe.ParsingFailure(log"Couldn't parse [\$input] as config", e))
    }
  }

}

final case class ConfigParsingError(path: String, expectedClass: String, error: Error)(implicit val pos: Position)
    extends BaseError {

  override def message: String = log"Couldn't load [\$expectedClass] at path [\$path]. Error [\${error.getMessage}]"

}
