package $organization$.persistence

$if(useMongo.truthy)$
import $organization$.persistence.mongo.MongoConfig
import $organization$.persistence.mongo.MongoError.UnhandledMongoError
$endif$
import $organization$.util.ResultT
$if(useMongo.truthy)$
import $organization$.util.config.ConfigOps._
import $organization$.util.logging.{Logging, TraceId}
import $organization$.util.logging.Loggable.InterpolatorOps._
import $organization$.util.TaskUtils
import $organization$.util.ResultT.deferEither
import monix.eval.Task
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoDatabase}
$else$
import $organization$.util.logging.{Logging, TraceId}
$endif$
import com.typesafe.config.Config

$if(useMongo.truthy)$
import scala.concurrent.TimeoutException
$endif$

trait PersistenceModuleLoader extends Logging {

  $if(useMongo.truthy)$
  def loadPersistenceModule(rootConfig: Config)(implicit traceId: TraceId): ResultT[PersistenceModule] = {
    for {
      mongoDatabase <- loadMongoModule(rootConfig)
    } yield PersistenceModule(mongoDatabase = mongoDatabase)
  }
  $else$
  def loadPersistenceModule(rootConfig: Config)(implicit traceId: TraceId): ResultT[PersistenceModule] = {
    import $organization$.util.ResultT.unit

    for {
      _ <- unit()
    } yield PersistenceModule()
  }
  $endif$


  $if(useMongo.truthy)$
  protected def loadMongoModule(rootConfig: Config)(implicit traceId: TraceId): ResultT[MongoDatabase] = {
    for {
      mongoConfig <- deferEither(rootConfig.load[MongoConfig]("application.persistence.mongodb"))

      _ = logger.info(log"Loading mongo module with config \$mongoConfig")

      db <- initializeMongoDatabase(mongoConfig)
    } yield db
  }

  protected def initializeMongoDatabase(mongoConfig: MongoConfig)(implicit traceId: TraceId): ResultT[MongoDatabase] = {
    import $organization$.util.ResultT.deferTask
    import $organization$.util.logging.Loggable.InterpolatorOps._

    def closeClientOnError[A](client: MongoClient, task: Task[A]): Task[A] = {
      task
        .doOnCancel(Task.eval(client.close()))
        .doOnFinish {
          case None    => Task.unit
          case Some(_) => Task.eval(client.close())
        }
    }

    val dbAsync = for {
      _ <- Task.eval(logger.info(log"Loading MongoDB module with config \$mongoConfig"))

      client <- Task.eval(MongoClient(mongoConfig.url.value))

      db <- closeClientOnError(client, Task.eval(client.getDatabase(mongoConfig.database.value).withCodecRegistry(DEFAULT_CODEC_REGISTRY)))

      _ = logger.info("Acquiring MongoDB connection")

      _ <- closeClientOnError(client, acquireMongoConnection(db, mongoConfig))
    } yield db

    deferTask(dbAsync, UnhandledMongoError.apply)
  }

  protected def acquireMongoConnection(db: MongoDatabase, mongoConfig: MongoConfig)(implicit traceId: TraceId): Task[Unit] = {
    import $organization$.util.logging.Loggable.InterpolatorOps._
    import scala.concurrent.duration._

    val connectionAttempt = Task
      .deferFuture(db.runCommand(BsonDocument("connectionStatus" -> 1)).toFutureOption())
      .asyncBoundary
      .timeoutTo(500.millis, Task.raiseError(new TimeoutException("Cannot acquire MongoDB connection in 500 millis")))

    val timeoutError = Task.raiseError[String](
      new TimeoutException(log"Cannot acquire MongoDB connection in [\${mongoConfig.retryPolicy.timeout}]")
    )

    for {
      _ <- TaskUtils.retry("Acquire MongoDB connection", connectionAttempt, mongoConfig.retryPolicy, timeoutError)
    } yield ()
  }
  $endif$

}

object PersistenceModuleLoader {

  object Default extends PersistenceModuleLoader

}