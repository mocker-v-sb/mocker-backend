package com.mocker.models.auth

import java.util.UUID

final case class User(id: UUID, username: String, password: String)
