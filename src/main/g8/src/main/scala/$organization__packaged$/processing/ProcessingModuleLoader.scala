package $organization$.processing

import $organization$.persistence.PersistenceModule
import $organization$.util.ResultT
import $organization$.util.ResultT.unit
import $organization$.util.logging.{Logging, TraceId}
import com.typesafe.config.Config

class ProcessingModuleLoader(rootConfig: Config, persistenceModule: PersistenceModule) extends Logging {

  def loadProcessingModule()(implicit traceId: TraceId): ResultT[ProcessingModule] = {
    for {
      _ <- unit()
    } yield new ProcessingModule()
  }

}
