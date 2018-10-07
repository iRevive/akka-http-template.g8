package $organization$.util
package logging

import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}

trait Logging {

  protected val logger: LoggerTakingImplicit[TraceId] =
    Logger.takingImplicit[TraceId](ClassUtils.getClassSimpleName(getClass))

}