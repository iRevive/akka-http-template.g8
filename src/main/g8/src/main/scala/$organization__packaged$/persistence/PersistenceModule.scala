package $organization$.persistence

$if(useMongo.truthy)$
import org.mongodb.scala.MongoDatabase
$endif$

$if(useMongo.truthy)$
final class PersistenceModule(val mongoDatabase: MongoDatabase)
$else$
final class PersistenceModule()
$endif$