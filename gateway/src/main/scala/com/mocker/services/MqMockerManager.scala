package com.mocker.services

import com.mocker.clients.MqMockerClientService
import com.mocker.models.mq.requests.{CreateTopicRequest, DeleteTopicRequest, GetMessagesRequest, GetTopicsRequest, SendMessageRequest}
import com.mocker.models.mq.responses.{CreateTopicResponse => ScalaCreateTopicResponse, DeleteTopicResponse => ScalaDeleteTopicResponse, GetMessagesResponse => ScalaGetMessagesResponse, GetTopicResponse => ScalaGetTopicResponse, GetTopicsResponse => ScalaGetTopicsResponse, SendMessageResponse => ScalaSendMessagesResponse}
import com.mocker.mq.mq_service.GetTopicsResponse
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.services.utils.StatusMapper
import io.grpc.{Status => GrpcStatus}
import zio.http.model.Method.{DELETE, GET, POST}
import zio.http._
import zio.http.model.{Status => HttpStatus}
import zio.http.{Http, Request, Response}
import zio.json.{DecoderOps, EncoderOps}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{Cause, Console, ZIO, ZLayer}

case class MqMockerManager(tracing: Tracing) {
  lazy val routes: Http[MqMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req@POST -> !! / "mq" / "topic" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[CreateTopicRequest])
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => MqMockerClientService.createTopic(request)
            case Left(error) => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse match {
            case Right(pResp) =>
              ZIO.succeed {
                Response
                  .json(ScalaCreateTopicResponse.fromMessage(pResp).toJson)
                  .setStatus(HttpStatus.Ok)
              }
            case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
          }
        } yield response

      case req@GET -> !! / "mq" / "messages" =>
        val brokerType = req.url.queryParams.get("brokerType").flatMap(_.headOption)
        val topicName = req.url.queryParams.get("topicName").flatMap(_.headOption)
        val repeat = req.url.queryParams.getOrElse("repeat", Seq("1")).head.toInt
        if (brokerType.isEmpty || topicName.isEmpty) ZIO.succeed(Response.status(HttpStatus.BadRequest))
        else {
          val request = GetMessagesRequest(brokerType = brokerType.get, topicName = topicName.get, repeat = repeat)
          for {
            protoResponse <- MqMockerClientService.getMessages(request).either
            response <- protoResponse match {
              case Right(pResp) =>
                ScalaGetMessagesResponse.fromMessage(pResp) match {
                  case Right(resp) => ZIO.succeed(Response.json(resp.toJson))
                  case Left(error) =>
                    ZIO.logErrorCause(Cause.fail(error)) *>
                      ZIO.succeed(Response.status(HttpStatus.InternalServerError))
                }
              case Left(errSt) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
            }
          } yield response
        }

      case req@POST -> !! / "mq" / "messages" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[SendMessageRequest])
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => MqMockerClientService.sendMessage(request)
            case Left(error) => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse match {
            case Right(pResp) =>
              ZIO.succeed {
                Response
                  .json(ScalaSendMessagesResponse.fromMessage(pResp).toJson)
                  .setStatus(HttpStatus.Ok)
              }
            case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
          }
        } yield response

      case req@GET -> !! / "mq" / "topics" =>
        val brokerType = req.url.queryParams.get("brokerType").flatMap(_.headOption)
        val searchInput = req.url.queryParams.get("search").flatMap(_.headOption)
        for {
          request <- ZIO.succeed(GetTopicsRequest(brokerType.getOrElse("ANY")))
          protoResponse <- MqMockerClientService.getTopics(request).either
          response <- protoResponse match {
            case Right(pResp) =>
              val _pResp = searchInput match {
                case Some(si) => GetTopicsResponse(queues = pResp.queues.filter(q => q.topicName.contains(si)))
                case None => pResp
              }
              ScalaGetTopicsResponse.fromMessage(_pResp) match {
                case Right(resp) =>
                  ZIO.succeed {
                    Response
                      .json(resp.toJson)
                      .setStatus(HttpStatus.Ok)
                  }
                case Left(error) =>
                  ZIO.logErrorCause(Cause.fail(error)) *>
                    ZIO.succeed(Response.status(HttpStatus.InternalServerError))
              }
            case Left(errSt) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
          }
        } yield response

      case req@DELETE -> !! / "mq" / "topic" =>
        val brokerType = req.url.queryParams.get("brokerType").flatMap(_.headOption)
        val topicName = req.url.queryParams.get("topicName").flatMap(_.headOption)
        if (brokerType.isEmpty || topicName.isEmpty) ZIO.succeed(Response.status(HttpStatus.BadRequest))
        else {
          val request = DeleteTopicRequest(brokerType = brokerType.get, topicName = topicName.get)
          for {
            protoResponse <- MqMockerClientService.deleteTopic(request).either
            response <- protoResponse match {
              case Right(pResp) => ZIO.succeed(Response.json(ScalaDeleteTopicResponse.fromMessage(pResp).toJson))
              case Left(errSt) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
            }
          } yield response
        }

      case req@GET -> !! / "mq" / "topic" =>
        val brokerType = req.url.queryParams.get("brokerType").flatMap(_.headOption)
        val topicName = req.url.queryParams.get("topicName").flatMap(_.headOption)
        if (brokerType.isEmpty || topicName.isEmpty) ZIO.succeed(Response.status(HttpStatus.BadRequest))
        else ZIO.succeed(Response.json(ScalaGetTopicResponse.fromMessage().copy(topicName = topicName.get).toJson))
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))
}

object MqMockerManager {
  def live = ZLayer.fromFunction(MqMockerManager.apply _)
}
