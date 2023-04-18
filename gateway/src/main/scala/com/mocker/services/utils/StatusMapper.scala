package com.mocker.services.utils

import io.grpc.{Status => GrpcStatus}
import zio.http.model.{Status => HttpStatus}

object StatusMapper {

  def grpc2Http(grpcStatus: GrpcStatus): HttpStatus = {
    if (grpcStatus.getCode == GrpcStatus.OK.getCode) HttpStatus.Ok
    else if (grpcStatus.getCode == GrpcStatus.INVALID_ARGUMENT.getCode) HttpStatus.BadRequest
    else if (grpcStatus.getCode == GrpcStatus.NOT_FOUND.getCode) HttpStatus.NotFound
    else if (grpcStatus.getCode == GrpcStatus.ALREADY_EXISTS.getCode) HttpStatus.Conflict
    else if (grpcStatus.getCode == GrpcStatus.INTERNAL.getCode) HttpStatus.InternalServerError
    else if (grpcStatus.getCode == GrpcStatus.UNIMPLEMENTED.getCode) HttpStatus.NotImplemented
    else HttpStatus.InternalServerError // TODO: complete mapper
  }
}
