package com.mocker.gateway.routes

import io.grpc.{Status => GrpcStatus}
import zhttp.http.{Status => HttpStatus}

object Grpc2HttpErrorMapper {
  def process(grpcStatus: GrpcStatus): HttpStatus = {
    case grpcStatus.getCode == GrpcStatus.OK => HttpStatus.Ok
    case grpcStatus.getCode == GrpcStatus.INVALID_ARGUMENT => HttpStatus.BadRequest
    case grpcStatus.getCode == GrpcStatus.NOT_FOUND => HttpStatus.NotFound
    case grpcStatus.getCode == GrpcStatus.ALREADY_EXISTS => HttpStatus.Conflict
    case grpcStatus.getCode == GrpcStatus.INTERNAL => HttpStatus.InternalServerError
    case grpcStatus.getCode == GrpcStatus.UNIMPLEMENTED => HttpStatus.NotImplemented
    case _ => HttpStatus.InternalServerError // TODO: complete mapper
  }
}
