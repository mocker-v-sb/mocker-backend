package com.mocker.gateway.routes

import com.mocker.clients.MqMockerClientService
import com.mocker.models.mq.requests.CreateTopicRequest._
import com.mocker.models.mq.requests.CreateTopicRequest
import com.mocker.models.mq.responses.CreateTopicResponse
import com.mocker.models.mq.responses.CreateTopicResponse._
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{GET, POST}
import zhttp.http.{!!, Http, Path, Request, Response, Status => HttpStatus}
import zio.{Console, ZIO}
import zio.json.{DecoderOps, EncoderOps}

object MockMqHandler {
  val prefix: Path = !! / "mq"

  lazy val routes: Http[MqMockerClient.Service, HttpStatus, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix / "createTopic" =>
      (for {
        request <- req.bodyAsString.map(_.fromJson[CreateTopicRequest])
          .tapError(err => Console.printError(err).ignoreLogged)
          .orElseFail(GrpcStatus.INTERNAL)
        protoResponse <- request match {
          case Right(request) => MqMockerClientService.createTopic(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }
      } yield Response.json(CreateTopicResponse.fromMessage(protoResponse).toJson)).mapError(Grpc2HttpErrorMapper.process)
  }
}
