package ru.mikhaildruzhinin.usermanagement

final case class UserBody(login: String, password: String)

final case class UserDto(login: String, password: String)
