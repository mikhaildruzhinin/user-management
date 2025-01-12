package ru.mikhaildruzhinin.usermanagement

import sttp.tapir._

import io.circe.generic.auto._
import scala.concurrent.Future
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Endpoints {

  val registerEndpoint: ServerEndpoint[Any, Future] = {
    endpoint.post
      .in("register")
      .in(jsonBody[UserBody])
      .out(jsonBody[UserDto])
      .serverLogicSuccess(x => Future.successful(UserDto(x.login, x.password)))
  }

  private val apiEndpoints: List[ServerEndpoint[Any, Future]] = {
    List(
      registerEndpoint
    )
  }

  private val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "user-management", "1.0.0")

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints
}
