package ru.mikhaildruzhinin.usermanagement

import pureconfig._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

case class Config(secretKey: String, host: String, port: Int)

object Config {

  implicit def hint[A]: ProductHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))

  def apply(): Config = ConfigSource.defaultApplication.loadOrThrow[Config]
}
