package ru.mikhaildruzhinin.usermanagement

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import pdi.jwt._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Endpoints {

  private val key = "changeMe"
  private val algorithm = JwtAlgorithm.HS256

  private val security = (token: AuthenticationToken) => {
    val principal = for {
      claim <- Future.fromTry(JwtCirce.decode(token.token, key, Seq(algorithm)))
    } yield Right(decode[UserDto](claim.content))

    principal.recover { case _ =>
      Left(ErrorMessage("Not Authorized"))
    }
  }

  val registerEndpoint: ServerEndpoint[Any, Future] = endpoint.post
    .in("register")
    .in(jsonBody[UserBody])
    .out(jsonBody[UserDto])
    .serverLogicSuccess(user => Future.successful(UserDto(user.login)))

  val loginEndpoint: ServerEndpoint[Any, Future] = endpoint.post
    .in("login")
    .in(jsonBody[UserBody])
    .out(jsonBody[AuthenticationToken])
    .errorOut(jsonBody[ErrorMessage])
    .serverLogic(user => {
      if (user.password == "admin") {
        val claim = JwtClaim(
          expiration = Some(Instant.now.plus(10L, ChronoUnit.MINUTES).getEpochSecond),
          issuedAt = Some(Instant.now.getEpochSecond),
          content = UserDto(user.login).asJson.noSpaces
        )
        val token = JwtCirce.encode(claim, key, algorithm)
        Future.successful(Right(AuthenticationToken(token)))
      } else Future.successful(Left(ErrorMessage("Authentication failed")))
    })

  val testEndpoint: ServerEndpoint[Any, Future] = endpoint.get
    .in("test")
    .out(jsonBody[Message])
    .errorOut(jsonBody[ErrorMessage])
    .securityIn(auth.bearer[String]().mapTo[AuthenticationToken])
    .serverSecurityLogic(security)
    .serverLogicSuccess(_ => _ => Future.successful(Message("ok")))

  private val apiEndpoints: List[ServerEndpoint[Any, Future]] = {
    List(registerEndpoint, loginEndpoint, testEndpoint)
  }

  private val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "user-management", "1.0.0")

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints
}
