package $organization$.util
package json

import java.nio.charset.Charset

import cats.data.NonEmptyList
import io.circe.{DecodingFailure, Json, ParsingFailure}

sealed trait JsonParsingError extends BaseError

object JsonParsingError {

  import $organization$.util.logging.Loggable.InterpolatorOps._

  final case class UnsupportedString(input: Array[Byte], charset: Charset, cause: Throwable)(implicit val pos: Position)
      extends JsonParsingError
      with ThrowableError {

    override lazy val message: String =
      log"Not able to create a string from byte array. Array length [\${input.length}]. Codec [\${charset.displayName()}]"

  }

  final case class NonParsableJson(input: String, failure: ParsingFailure)(implicit val pos: Position)
      extends JsonParsingError {

    override lazy val message: String = log"Could not parse json from input [\$input]. Error: [\${failure.message}]"

  }

  final case class JsonDecodingError(json: Json, className: String, errors: NonEmptyList[DecodingFailure])(
      implicit val pos: Position)
      extends JsonParsingError {

    override lazy val message: String = {
      val errorMessage = errors.toList.map(_.getMessage()).mkString(", ")

      log"Could not parse json as [\$className] from input [\${json.noSpaces}]. Errors: [\$errorMessage]"
    }

  }

}
