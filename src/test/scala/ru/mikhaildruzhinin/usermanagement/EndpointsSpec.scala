package ru.mikhaildruzhinin.usermanagement

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt._
import ru.mikhaildruzhinin.usermanagement.Endpoints._
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.testing.SttpBackendStub
import sttp.model._
import sttp.tapir.server._
import sttp.tapir.server.stub.TapirStubInterpreter

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class EndpointsSpec extends AsyncFlatSpec with Matchers with EitherValues {

  private val baseUriStub = "http://test.com"

  private val backendStub = (endpoint: ServerEndpoint[Any, Future]) => {
    TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(endpoint)
      .backend()
  }

  // @formatter:off
  /**
   * @tparam A successful response model
   */
  private def sendRequest[A](method: Method,
                             uri: String,
                             token: AuthenticationToken)
                            (implicit decoder: Decoder[A],
                             endpoint: ServerEndpoint[Any, Future]) = {

    basicRequest
      .method(method, uri"$baseUriStub/${getPathSegments(uri)}")
      .headers(Map("Authorization" -> s"Bearer ${token.token}"))
      .response(asJson[A])
      .send(backendStub(endpoint))
  }

  /**
   * @tparam A request body model
   * @tparam B successful response model
   */
  private def sendRequest[A, B](method: Method, uri: String, body: A)
                               (implicit encoder: Encoder[A],
                                decoder: Decoder[B],
                                endpoint: ServerEndpoint[Any, Future]) = {

    basicRequest
      .method(method, uri"$baseUriStub/${getPathSegments(uri)}")
      .body[A](body)
      .response(asJson[B])
      .send(backendStub(endpoint))
  }
  // @formatter:on

  private def getPathSegments(uri: String): Seq[String] = {
    uri.split("/").toSeq match {
      case x@_ if x.headOption.contains("") => x.tail
      case x@_ => x
    }
  }

  it should "register" in {

    implicit val endpoint: ServerEndpoint[Any, Future] = registerEndpoint
    val body = UserBody("admin", "admin")

    val response = sendRequest[UserBody, UserDto](
      method = Method.POST,
      uri = "register",
      body = body
    )

    response.map(_.body.value shouldBe UserDto("admin"))
  }

  it should "successful login" in {

    implicit val endpoint: ServerEndpoint[Any, Future] = loginEndpoint
    val body = UserBody("admin", "admin")

    val response = sendRequest[UserBody, AuthenticationToken](
      method = Method.POST,
      uri = "login",
      body = body
    )

    response.map(_.body.value shouldBe an[AuthenticationToken])
  }

  it should "unsuccessful login" in {

    implicit val endpoint: ServerEndpoint[Any, Future] = loginEndpoint
    val body = UserBody("not_admin", "not_admin")

    lazy val response = sendRequest[UserBody, AuthenticationToken](
      method = Method.POST,
      uri = "login",
      body = body
    )

    response.map(_.code shouldBe StatusCode.BadRequest)
  }

  it should "successful test" in {

    val claim = JwtClaim(
      expiration = Some(Instant.now.plus(10L, ChronoUnit.MINUTES).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = UserDto("admin").asJson.noSpaces
    )
    val key = Config().secretKey
    val algorithm = JwtAlgorithm.HS256
    val token = AuthenticationToken(JwtCirce.encode(claim, key, algorithm))
    implicit val endpoint: ServerEndpoint[Any, Future] = testEndpoint

    val response = sendRequest[Message](
      method = Method.GET,
      uri = "test",
      token = token
    )

    response.map(_.body.value shouldBe Message("ok"))
  }

  it should "unsuccessful test" in {

    val claim = JwtClaim(
      expiration = Some(Instant.now.plus(10L, ChronoUnit.MINUTES).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = UserDto("admin").asJson.noSpaces
    )
    val key = "wrong_key"
    val algorithm = JwtAlgorithm.HS256
    val token = AuthenticationToken(JwtCirce.encode(claim, key, algorithm))
    implicit val endpoint: ServerEndpoint[Any, Future] = testEndpoint

    val response = sendRequest[Message](
      method = Method.GET,
      uri = "test",
      token = token
    )

    response.map(_.code shouldBe StatusCode.BadRequest)
  }
}
