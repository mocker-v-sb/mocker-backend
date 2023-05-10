package com.mocker.models.rest.utils

import com.mocker.common.auth.Authorization

object AuthUtils {

  def buildAuthorization(user: String): Authorization = {
    Authorization(user = user)
  }
}
