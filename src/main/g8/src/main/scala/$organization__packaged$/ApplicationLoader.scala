package $organization$

import $organization$.api.{ApiModule, ApiModuleLoader}
import $organization$.persistence.{PersistenceModule, PersistenceModuleLoader}
import $organization$.processing.{ProcessingModule, ProcessingModuleLoader}
import $organization$.util.logging.{Logging, TraceId}
import $organization$.util.ResultT
import $organization$.util.ResultT.safe
import com.typesafe.config.{Config, ConfigFactory}

trait ApplicationLoader extends Logging {

  import ApplicationLoader._

  def loadApplication()(implicit traceId: TraceId): ResultT[Application] = {
    for {
      config            <- loadConfig()
      persistenceModule <- persistenceModuleLoader(config).loadPersistenceModule()
      processingModule  <- processingModuleLoader(config, persistenceModule).loadProcessingModule()
      apiModule         <- apiModuleLoader(config).loadApiModule()

      application = new Application(
        persistenceModule = persistenceModule,
        processingModule = processingModule,
        apiModule = apiModule
      )
    } yield application
  }

  protected def loadConfig(): ResultT[Config] = safe(ConfigFactory.load())

  protected def persistenceModuleLoader(config: Config): PersistenceModuleLoader = {
    new PersistenceModuleLoader(config)
  }

  protected def apiModuleLoader(config: Config): ApiModuleLoader = {
    new ApiModuleLoader(config)
  }

  protected def processingModuleLoader(config: Config, persistenceModule: PersistenceModule): ProcessingModuleLoader = {
    new ProcessingModuleLoader(config, persistenceModule)
  }

}

object ApplicationLoader {

  object Default extends ApplicationLoader

  final class Application(
      val persistenceModule: PersistenceModule,
      val processingModule: ProcessingModule,
      val apiModule: ApiModule
  )

}
