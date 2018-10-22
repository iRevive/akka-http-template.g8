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
      persistenceModule <- persistenceModuleLoader.loadPersistenceModule(config)
      processingModule  <- processingModuleLoader.loadProcessingModule(config, persistenceModule)
      apiModule         <- apiModuleLoader.loadApiModule(config)

      application = new Application(
        persistenceModule = persistenceModule,
        processingModule = processingModule,
        apiModule = apiModule
      )
    } yield application
  }

  protected def loadConfig(): ResultT[Config] = safe(ConfigFactory.load())

  protected def persistenceModuleLoader: PersistenceModuleLoader = PersistenceModuleLoader.Default

  protected def apiModuleLoader: ApiModuleLoader = ApiModuleLoader.Default

  protected def processingModuleLoader: ProcessingModuleLoader = ProcessingModuleLoader.Default

}

object ApplicationLoader {

  object Default extends ApplicationLoader

  final class Application(
      val persistenceModule: PersistenceModule,
      val processingModule: ProcessingModule,
      val apiModule: ApiModule
  )

}
