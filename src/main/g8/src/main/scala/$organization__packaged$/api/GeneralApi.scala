package $organization$.api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

class GeneralApi {

  lazy val routes: Route =
    path("api" / "health") {
      get {
        complete((StatusCodes.OK, "I'm alive"))
      }
    }

}
