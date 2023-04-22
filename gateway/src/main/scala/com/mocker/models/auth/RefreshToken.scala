package com.mocker.models.auth

import java.util.UUID

final case class RefreshToken(id: UUID, token: String)
