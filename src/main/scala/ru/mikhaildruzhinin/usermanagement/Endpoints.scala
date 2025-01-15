package ru.mikhaildruzhinin.usermanagement

import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import pdi.jwt._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

object Endpoints {

  val registerEndpoint: ServerEndpoint[Any, Future] = endpoint.post
    .in("register")
    .in(jsonBody[UserBody])
    .out(jsonBody[UserDto])
    .serverLogicSuccess(user => Future.successful(UserDto(user.login)))

  val loginEndpoint: ServerEndpoint[Any, Future] = endpoint.post
    .in("login")
    .in(jsonBody[UserBody])
    .out(jsonBody[AuthenticationToken])
    .errorOut(jsonBody[LoginError])
    .serverLogic(user => {
      if (user.login == "admin") {
        val claim = JwtClaim(
          expiration = Some(Instant.now.plus(10L, ChronoUnit.MINUTES).getEpochSecond),
          issuedAt = Some(Instant.now.getEpochSecond),
          content = UserDto(user.login).asJson.noSpaces
        )
        val key = "changeMe"
        val algorithm = JwtAlgorithm.HS256
        val token = JwtCirce.encode(claim, key, algorithm)
        Future.successful(Right(AuthenticationToken(token)))
      } else Future.successful(Left(LoginError("Authentication failed")))
    })

  private val apiEndpoints: List[ServerEndpoint[Any, Future]] = {
    List(registerEndpoint, loginEndpoint)
  }

  private val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "user-management", "1.0.0")

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints
}
