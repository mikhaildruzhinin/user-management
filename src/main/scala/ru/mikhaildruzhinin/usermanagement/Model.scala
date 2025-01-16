package ru.mikhaildruzhinin.usermanagement

final case class UserBody(login: String, password: String)

final case class UserDto(login: String)

final case class AuthenticationToken(token: String)

final case class Message(message: String)

final case class ErrorMessage(message: String)
