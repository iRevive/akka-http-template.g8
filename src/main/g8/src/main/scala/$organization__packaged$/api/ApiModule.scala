package $organization$.api

import akka.http.scaladsl.server.Route

final class ApiModule(val routes: Route, val config: ApiConfig)
