package $organization$.api

import akka.http.scaladsl.server.Route

case class ApiModule(routes: Route, config: ApiConfig)