package com.mocker.gateway.routes

import com.mocker.clients.MqMockerClientService
import com.mocker.models.mq.requests.CreateTopicRequest._
import com.mocker.models.mq.requests.{CreateTopicRequest, GetMessagesRequest}
import com.mocker.models.mq.responses.{
  CreateTopicResponse => ScalaCreateTopicResponse,
  GetMessagesResponse => ScalaGetMessagesResponse}
import com.mocker.models.mq.responses.CreateTopicResponse._
import com.mocker.mq.mq_service.{CreateTopicResponse => ProtoCreateTopicResponse, GetMessagesResponse => ProtoGetMessagesResponse}
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{GET, POST}
import zhttp.http.{!!, ->, /, Http, Path, Request, Response, Status => HttpStatus}
import zio.{Console, ZIO}
import zio.json.{DecoderOps, EncoderOps}

object MockMqHandler {
  val prefix: Path = !! / "mq"

  lazy val routes: Http[MqMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix / "createTopic" =>
      for {
        request <- req.bodyAsString.map(_.fromJson[CreateTopicRequest])
          .tapError(err => Console.printError(err).ignoreLogged)
        protoResponse <- (request match {
          case Right(request) => MqMockerClientService.createTopic(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }).either
        response <- protoResponse match {
          case Right(pResp: ProtoCreateTopicResponse) => ZIO.succeed {
            Response
              .json(ScalaCreateTopicResponse.fromMessage(pResp).toJson)
              .setStatus(HttpStatus.Ok)
          }
          case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
        }
      } yield response
    case req @ GET -> prefix / "messages" =>
      val brokerType = req.url.queryParams.get("brokerType").flatMap(_.headOption)
      val topicName = req.url.queryParams.get("topicName").flatMap(_.headOption)
      val repeat = req.url.queryParams.getOrElse("repeat", Seq("1")).head.toInt
      if (brokerType.isEmpty || topicName.isEmpty) ZIO.succeed(Response.status(HttpStatus.BadRequest))
      else {
        val request = GetMessagesRequest(brokerType = brokerType.get, topicName = topicName.get, repeat = repeat)
        for {
          protoResponse <- MqMockerClientService.getMessages(request).either
          response <- protoResponse match {
            case Right(pResp) => ScalaGetMessagesResponse.fromMessage(pResp) match {
              case Right(resp) => ZIO.succeed(Response.json(resp.toJson))
              case Left(error) => Console.printError(error).ignoreLogged *>
                ZIO.succeed(Response.status(HttpStatus.InternalServerError))
            }
            case Left(errSt) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
          }
        } yield response
      }
  }
}
