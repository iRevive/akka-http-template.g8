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
import $organization$.util.ResultT.{evalEither, deferTask}
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

class PersistenceModuleLoader(rootConfig: Config) extends Logging {

  $if(useMongo.truthy)$
  def loadPersistenceModule()(implicit traceId: TraceId): ResultT[PersistenceModule] = {
    for {
      mongoDatabase <- loadMongoDatabase()
    } yield new PersistenceModule(mongoDatabase)
  }
  $else$
  def loadPersistenceModule()(implicit traceId: TraceId): ResultT[PersistenceModule] = {
    import $organization$.util.ResultT.unit

    for {
      _ <- unit()
    } yield new PersistenceModule()
  }
  $endif$


  $if(useMongo.truthy)$
  private[persistence] def loadMongoDatabase()(implicit traceId: TraceId): ResultT[MongoDatabase] = {
    for {
      mongoConfig <- evalEither(rootConfig.load[MongoConfig]("application.persistence.mongodb"))

      _ = logger.info(log"Loading mongo module with config \$mongoConfig")

      db <- initializeMongoDatabase(mongoConfig)
    } yield db
  }

  protected def initializeMongoDatabase(config: MongoConfig)(implicit traceId: TraceId): ResultT[MongoDatabase] = {
    def closeClientOnError[A](client: MongoClient, task: Task[A]): Task[A] = {
      task
        .doOnCancel(Task.eval(client.close()))
        .doOnFinish {
          case None    => Task.unit
          case Some(_) => Task.eval(client.close())
        }
    }

    val dbAsync = for {
      _ <- Task.eval(logger.info(log"Loading MongoDB module with config \$config"))

      client <- Task.eval(MongoClient(config.uri.value))

      db <- closeClientOnError(
        client,
        Task.eval(client.getDatabase(config.database.value).withCodecRegistry(DEFAULT_CODEC_REGISTRY))
      )

      timeoutError = Task.raiseError[String](
        new TimeoutException(log"Cannot acquire MongoDB connection in [\${config.retryPolicy.timeout}]")
      )

      _ = logger.info("Acquiring MongoDB connection")

      _ <- closeClientOnError(
        client,
        TaskUtils.retry("Acquire MongoDB connection", mongoConnectionAttempt(db, config), config.retryPolicy, timeoutError)
      )
    } yield db

    deferTask(dbAsync, UnhandledMongoError.apply)
  }

  private[persistence] def mongoConnectionAttempt(db: MongoDatabase, config: MongoConfig): Task[Unit] = {
    val timeoutTo = Task.raiseError(
      new TimeoutException(log"Failed attempt to acquire MongoDB connection in [\${config.connectionAttemptTimeout}]")
    )

    Task
      .deferFuture(db.runCommand(BsonDocument("connectionStatus" -> 1)).toFutureOption())
      .asyncBoundary
      .map(_ => ())
      .timeoutTo(config.connectionAttemptTimeout, timeoutTo)
  }
  $endif$

}
