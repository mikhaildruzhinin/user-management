package ru.mikhaildruzhinin.usermanagement

import org.slf4j.{Logger, LoggerFactory}
import sttp.tapir.server.netty._

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Main {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  private val config: Config = Config()

  private val startingLogMessage = (binding: NettyFutureServerBinding) => {
    s"Go to http://${binding.localSocket.getHostString}:${binding.port}/docs to open SwaggerUI"
  }

  def main(args: Array[String]): Unit = {
    for {
      binding <- NettyFutureServer(
        Config.nettyFutureServerOptions,
        NettyConfig.default.withGracefulShutdownTimeout(Duration(5L, TimeUnit.SECONDS))
      )
        .host(config.host)
        .port(config.port)
        .addEndpoints(Endpoints.all)
        .start()
      _ = log.info(startingLogMessage(binding))
      _ = sys.addShutdownHook {
        log.info(s"Shutting down Netty server.")
        binding.stop()
        log.info(s"Netty server stopped.")
      }
    } yield ()
  }
}
