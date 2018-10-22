package $organization$.util

import cats.data.EitherT
import cats.syntax.either.{catsSyntaxEither, catsSyntaxEitherObject}
import monix.eval.Task

import scala.concurrent.Future
import scala.util.control.NonFatal

trait ResultTOps {

  @inline
  final def unit(): ResultT[Unit] =
    eval(())

  @inline
  final def leftT[A](value: => BaseError): ResultT[A] =
    evalEither(Left(value))

  @inline
  def eval[A](value: => A): ResultT[A] =
    evalEither(Right(value))

  @inline
  def deferFuture[A](value: => Future[A], errorWrapper: Throwable => ThrowableError = ThrowableError.apply)(
      implicit pos: Position): ResultT[A] = {
    deferTask(Task.deferFuture(value).asyncBoundary, errorWrapper)
  }

  @inline
  def deferTask[A](value: => Task[A], errorWrapper: Throwable => ThrowableError = ThrowableError.apply)(
      implicit pos: Position): ResultT[A] = {
    safe(value).flatMapF { task =>
      task
        .map[Either[BaseError, A]](Right.apply)
        .onErrorRecover { case NonFatal(e) => Left(errorWrapper(e)) }
    }
  }

  @inline
  def safe[A](value: => A)(implicit pos: Position): ResultT[A] =
    safe(value, ThrowableError.apply)

  @inline
  def safe[A](value: => A, errorWrapper: Throwable => ThrowableError): ResultT[A] =
    evalEither(Either.catchNonFatal(value).leftMap(errorWrapper))

  @inline
  def evalEither[E <: BaseError, A](value: => Either[E, A]): ResultT[A] =
    EitherT(Task.eval[Either[BaseError, A]](value))

}
