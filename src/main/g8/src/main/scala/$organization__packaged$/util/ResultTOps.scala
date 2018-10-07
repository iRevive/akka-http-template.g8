package $organization$.util

import cats.data.EitherT
import cats.syntax.either.{catsSyntaxEither, catsSyntaxEitherObject}
import monix.eval.Task

import scala.concurrent.Future
import scala.util.control.NonFatal

trait ResultTOps {

  @inline
  final def unit(): ResultT[Unit] =
    defer(())

  @inline
  final def leftT[A](value: => BaseError): ResultT[A] =
    deferEither(Left(value))

  @inline
  def defer[A](value: => A): ResultT[A] =
    deferEither(Right(value))

  @inline
  def deferFuture[A](value: => Future[A], errorWrapper: Throwable => ThrowableError = ThrowableError.apply)
                    (implicit pos: Position): ResultT[A] = {
    deferTask(Task.deferFuture(value).asyncBoundary, errorWrapper)
  }

  @inline
  def deferTask[A](value: => Task[A], errorWrapper: Throwable => ThrowableError = ThrowableError.apply)
                  (implicit pos: Position): ResultT[A] = {
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
    deferEither(Either.catchNonFatal(value).leftMap(errorWrapper))

  @inline
  def deferEither[E <: BaseError, A](value: => Either[E, A]): ResultT[A] =
    EitherT(Task.eval[Either[BaseError, A]](value))

}