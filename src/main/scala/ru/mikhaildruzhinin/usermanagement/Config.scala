package ru.mikhaildruzhinin.usermanagement

import org.slf4j.{Logger, LoggerFactory}
import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.netty.NettyFutureServerOptions

import scala.concurrent.Future

case class Config(secretKey: String, host: String, port: Int)

object Config {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  private val serverLog: DefaultServerLog[Future] = {
    NettyFutureServerOptions
      .defaultServerLog
      .doLogWhenHandled((msg, _) => Future.successful(log.info(msg)))
  }

  val nettyFutureServerOptions: NettyFutureServerOptions = {
    NettyFutureServerOptions.customiseInterceptors
      .serverLog(serverLog)
      .options
  }

  implicit def hint[A]: ProductHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))

  def apply(): Config = ConfigSource.defaultApplication.loadOrThrow[Config]
}
