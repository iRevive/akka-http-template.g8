package $organization$

import cats.data.EitherT
import monix.eval.Task

package object util {

  type ResultT[A] = EitherT[Task, BaseError, A]

  type BaseErrorOr[A] = Either[BaseError, A]

  object ResultT extends ResultTOps

}
