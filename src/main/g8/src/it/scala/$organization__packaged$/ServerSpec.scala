package $organization$

import akka.actor.ActorSystem
import akka.testkit.TestKit
import $organization$.it.ITSpec
import $organization$.util.ResultT
import $organization$.util.ResultT.{deferFuture, eval}
import com.typesafe.config.{Config, ConfigFactory}

class ServerSpec extends ITSpec {

  "Server" should {

    "stop system in case of error during initialization" in withSystem { implicit system =>
      val loader = new ApplicationLoader {
        override protected def loadConfig(): ResultT[Config] = {
          eval(ConfigFactory.empty())
        }
      }

      val error = intercept[Exception](Server.startApp(loader).runSyncUnsafe(DefaultTimeout))

      $if(useMongo.truthy)$
      val expectedMessage = "ConfigParsingError(" +
        "message = Couldn't load [MongoConfig] at path [application.persistence.mongodb]. " +
        "Error [Path not found in config], " +
        "pos = $organization$.util.config.RichConfig#load:25)"
      $else$
      val expectedMessage = "ConfigParsingError(" +
        "message = Couldn't load [ApiConfig] at path [application.api]. " +
        "Error [Path not found in config], " +
        "pos = $organization$.util.config.RichConfig#load:25)"
      $endif$


      error.getMessage shouldBe expectedMessage

      deferFuture(system.whenTerminated).runSyncUnsafe() shouldBe right
    }

    "load application" in withSystem { implicit system =>
      import scala.concurrent.duration._

      val loader = ApplicationLoader.Default

      noException shouldBe thrownBy(Server.startApp(loader).runSyncUnsafe(40.seconds))
    }

  }

  private def withSystem(op: ActorSystem => Any): Any = {
    val system: ActorSystem = ActorSystem()
    try {
      op(system)
    } finally {
      TestKit.shutdownActorSystem(system)
    }
  }

}
