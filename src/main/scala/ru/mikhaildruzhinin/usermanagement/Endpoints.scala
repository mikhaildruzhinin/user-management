package ru.mikhaildruzhinin.usermanagement

import sttp.tapir._

import io.circe.generic.auto._
import scala.concurrent.Future
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Endpoints {

  import Library._

  private final case class User(name: String)

  val helloServerEndpoint: ServerEndpoint[Any, Future] = {
    endpoint.get
      .in("hello")
      .in(query[String]("name").mapTo[User])
      .out(stringBody)
      .serverLogicSuccess(user => Future.successful(s"Hello ${user.name}"))
  }

  val booksListingServerEndpoint: ServerEndpoint[Any, Future] = {
    endpoint.get
      .in("books" / "list" / "all")
      .out(jsonBody[Seq[Book]])
      .serverLogicSuccess(_ => Future.successful(Library.books))
  }

  private val apiEndpoints: List[ServerEndpoint[Any, Future]] = {
    List(helloServerEndpoint, booksListingServerEndpoint)
  }

  private val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "user-management", "1.0.0")

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints
}

object Library {

  final case class Author(name: String)

  final case class Book(title: String, year: Int, author: Author)

  val books: Seq[Book] = Seq(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
}
