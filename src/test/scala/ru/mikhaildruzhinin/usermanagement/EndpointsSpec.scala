package ru.mikhaildruzhinin.usermanagement

import io.circe.Decoder
import io.circe.generic.auto._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.mikhaildruzhinin.usermanagement.Endpoints._
import ru.mikhaildruzhinin.usermanagement.Library._
import sttp.capabilities.WebSockets
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

  // @formatter:off
  private def sendRequest[A](method: Method, uri: String,
                             backend: SttpBackend[Future, WebSockets])
                            (implicit decoder: Decoder[A]) = {

    basicRequest
      .method(method, uri"$baseUriStub/${uri.split("/").toSeq}")
      .response(asJson[A])
      .send(backend)
  }
  // @formatter:on

  it should "return hello message" in {
    val endpoint = helloServerEndpoint
    // given
    val backend: SttpBackend[Future, WebSockets] = backendStub(endpoint)

    // when
    val response = basicRequest
      .get(uri"$baseUriStub/hello?name=adam")
      .send(backend)

    // then
    response.map(_.body.value shouldBe "Hello adam")
  }

  it should "list available books" in {
    // given
    val endpoint = booksListingServerEndpoint

    // when
    val response = sendRequest[Seq[Book]](
      method = Method.GET,
      uri = "books/list/all",
      backend = backendStub(endpoint)
    )

    // then
    response.map(_.body.value shouldBe books)
  }
}
