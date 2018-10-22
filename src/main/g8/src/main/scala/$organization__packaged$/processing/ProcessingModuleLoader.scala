package $organization$.processing

import $organization$.persistence.PersistenceModule
import $organization$.util.ResultT
import $organization$.util.ResultT.unit
import $organization$.util.logging.{Logging, TraceId}
import com.typesafe.config.Config

trait ProcessingModuleLoader extends Logging {

  def loadProcessingModule(rootConfig: Config, persistenceModule: PersistenceModule)
                          (implicit traceId: TraceId): ResultT[ProcessingModule] = {
    for {
      _ <- unit()
    } yield new ProcessingModule()
  }

}

object ProcessingModuleLoader {

  object Default extends ProcessingModuleLoader

}