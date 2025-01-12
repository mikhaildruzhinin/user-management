package ru.mikhaildruzhinin.usermanagement

import org.slf4j.{Logger, LoggerFactory}
import sttp.tapir.server.netty._

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Main {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    for {
      binding <- NettyFutureServer(
        NettyFutureServerOptions.default,
        NettyConfig.default.withGracefulShutdownTimeout(Duration(5L, TimeUnit.SECONDS))
      )
        .port(8080)
        .addEndpoints(Endpoints.all)
        .start()
      _ = log.info(s"Go to http://localhost:${binding.port}/docs to open SwaggerUI")
      _ = sys.addShutdownHook {
        log.info(s"Shutting down Netty server.")
        binding.stop()
        log.info(s"Netty server stopped.")
      }
    } yield ()
  }
}
