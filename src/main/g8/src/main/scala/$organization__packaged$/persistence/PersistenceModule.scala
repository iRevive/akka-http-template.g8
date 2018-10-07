package $organization$.persistence

$if(useMongo.truthy)$
import org.mongodb.scala.MongoDatabase
$endif$

$if(useMongo.truthy)$
case class PersistenceModule(mongoDatabase: MongoDatabase)
$else$
case class PersistenceModule()
$endif$