package ru.mikhaildruzhinin.usermanagement

import ru.mikhaildruzhinin.usermanagement.Endpoints._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter

import Library._
import io.circe.generic.auto._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import sttp.client3.circe._

class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "return hello message" in {
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(helloServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/hello?name=adam")
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe "Hello adam").unwrap
  }

  it should "list available books" in {
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(booksListingServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/books/list/all")
      .response(asJson[List[Book]])
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe books).unwrap
  }

  implicit class Unwrapper[T](t: Future[T]) {
    def unwrap: T = Await.result(t, Duration.Inf)
  }
}
