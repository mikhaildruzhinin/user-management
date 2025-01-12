package ru.mikhaildruzhinin.usermanagement

import io.circe._
import io.circe.generic.auto._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.mikhaildruzhinin.usermanagement.Endpoints._
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.tapir.server._
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.concurrent.Future

class EndpointsSpec extends AsyncFlatSpec with Matchers with EitherValues {

  private val baseUriStub = "http://test.com"

  private val backendStub = (endpoint: ServerEndpoint[Any, Future]) => {
    TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(endpoint)
      .backend()
  }

  /**
   * @tparam A successful response model
   */
  // @formatter:off
  private def sendRequest[A](method: Method,
                             uri: String)
                            (implicit decoder: Decoder[A],
                             endpoint: ServerEndpoint[Any, Future]) = {

    basicRequest
      .method(method, uri"$baseUriStub/${getPathSegments(uri)}")
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

      // given
      implicit val endpoint: ServerEndpoint[Any, Future] = registerEndpoint
      val body = UserBody("admin", "admin")

      // when
      val response = sendRequest[UserBody, UserDto](
        method = Method.POST,
        uri = "register",
        body = body
      )

      // then
      response.map(_.body.value shouldBe UserDto("admin", "admin"))
    }
}
