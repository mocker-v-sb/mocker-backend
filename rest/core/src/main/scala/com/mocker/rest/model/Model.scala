package com.mocker.rest.model

import com.mocker.rest.model.ResponseTypeNamespace.ResponseType

case class Model(
    id: Long = 0,
    serviceId: Long = 0,
    name: String,
    description: Option[String],
    responseType: ResponseType,
    response: String
)
