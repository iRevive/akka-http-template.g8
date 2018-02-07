package $organization$.api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

class Endpoints {

  lazy val routes: Route =
    path("api" / "health") {
      get {
        complete((StatusCodes.OK, "I'm alive"))
      }
    }

}