package $organization$.util

import com.typesafe.config.Config
import io.circe.{Decoder, Error}

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait ConfigOps {

  implicit def toRichConfig(config: Config): RichConfig = new RichConfig(config)

}

final class RichConfig(private val config: Config) extends AnyVal {

  import io.circe.config.syntax._

  def loadUnsafe[A: Decoder : ClassTag](path: String): A = {
    config.as[A](path).fold(
      error => throw new IllegalArgumentException(formatMessage[A](path, error), error),
      identity
    )
  }

  private def formatMessage[A](path: String, error: Error)(implicit ct: ClassTag[A]): String = {
    val className = implicitly[ClassTag[A]].runtimeClass.getSimpleName.stripSuffix("\$")

    s"Couldn't load [\$className] at path [\$path]. Error [\${error.getMessage}]"
  }

}