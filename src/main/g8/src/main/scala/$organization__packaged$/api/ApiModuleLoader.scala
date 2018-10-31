package $organization$.api

import $organization$.util.ResultT
import $organization$.util.ResultT.evalEither
import $organization$.util.config.ConfigOps._
import $organization$.util.logging.Loggable.InterpolatorOps._
import $organization$.util.logging.{Logging, TraceId}
import com.typesafe.config.Config

class ApiModuleLoader(rootConfig: Config) extends Logging {

  def loadApiModule()(implicit traceId: TraceId): ResultT[ApiModule] = {
    for {
      apiConfig <- evalEither(rootConfig.load[ApiConfig]("application.api"))

      _ = logger.info(log"Loading API module with config \$apiConfig")

      api = new GeneralApi()
    } yield new ApiModule(api.routes, apiConfig)
  }

}
