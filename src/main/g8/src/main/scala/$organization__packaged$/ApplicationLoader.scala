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

  def loadModules()(implicit traceId: TraceId): ResultT[ApplicationModules] = {
    for {
      config <- loadConfig()

      persistenceModule <- persistenceModuleLoader.loadPersistenceModule(config)

      processingModule <- processingModuleLoader.loadProcessingModule(config, persistenceModule)

      apiModule <- apiModuleLoader.loadApiModule(config)

      applicationModules = ApplicationModules(
        persistenceModule = persistenceModule,
        processingModule = processingModule,
        apiModule = apiModule
      )
    } yield applicationModules
  }

  protected def loadConfig(): ResultT[Config] = safe(ConfigFactory.load())

  protected def persistenceModuleLoader: PersistenceModuleLoader = PersistenceModuleLoader.Default

  protected def apiModuleLoader: ApiModuleLoader = ApiModuleLoader.Default

  protected def processingModuleLoader: ProcessingModuleLoader = ProcessingModuleLoader.Default

}

object ApplicationLoader {

  object Default extends ApplicationLoader

  case class ApplicationModules(persistenceModule: PersistenceModule,
                                processingModule: ProcessingModule,
                                apiModule: ApiModule)

}