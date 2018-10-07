package $organization$.api

import $organization$.util.ResultT
import $organization$.util.ResultT.deferEither
import $organization$.util.config.ConfigOps._
import $organization$.util.logging.Loggable.InterpolatorOps._
import $organization$.util.logging.{Logging, TraceId}
import com.typesafe.config.Config

trait ApiModuleLoader extends Logging {

  def loadApiModule(rootConfig: Config)(implicit traceId: TraceId): ResultT[ApiModule] = {
    for {
      apiConfig <- deferEither(rootConfig.load[ApiConfig]("application.api"))

      _ = logger.info(log"Loading API module with config \$apiConfig")

      endpoints = new Endpoints()
    } yield ApiModule(endpoints.routes, apiConfig)
  }

}

object ApiModuleLoader {

  object Default extends ApiModuleLoader

}