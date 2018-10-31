package $organization$.api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import org.scalatest._

class GeneralApiSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "General API" should {

    "return 'I'm alive' from api/health endpoint" in {
      Get("/api/health") ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "I'm alive"
      }
    }

  }

  private lazy val routes: Route = new GeneralApi().routes

}
