package com.mocker.clients

import com.mocker.models.mq.requests.{
  CreateTopicRequest => ScalaCreateTopicRequest,
  DeleteTopicRequest => ScalaDeleteTopicRequest,
  GetMessagesRequest => ScalaGetMessagesRequest,
  GetTopicsRequest => ScalaGetTopicsRequest,
  SendMessageRequest => ScalaSendMessageRequest
}
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.mq.mq_service.{
  CreateTopicResponse,
  DeleteTopicResponse,
  GetMessagesResponse,
  GetTopicsResponse,
  SendMessageResponse
}
import io.grpc.Status
import zio.{Cause, Console, ZIO}

object MqMockerClientService {

  def createTopic(request: ScalaCreateTopicRequest): ZIO[MqMockerClient.Service, Status, CreateTopicResponse] =
    for {
      resp <- request.toMessage match {
        case Right(message) => MqMockerClient.createTopic(message)
        case Left(error)    => ZIO.logErrorCause(Cause.fail(error)).ignoreLogged *> ZIO.fail(Status.INVALID_ARGUMENT)
      }
    } yield resp

  def sendMessage(request: ScalaSendMessageRequest): ZIO[MqMockerClient.Service, Status, SendMessageResponse] =
    for {
      resp <- request.toMessage match {
        case Right(message) => MqMockerClient.sendMessage(message)
        case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(Status.INVALID_ARGUMENT)
      }
    } yield resp

  def getMessages(request: ScalaGetMessagesRequest): ZIO[MqMockerClient.Service, Status, GetMessagesResponse] =
    for {
      resp <- request.toMessage match {
        case Right(message) => MqMockerClient.getMessages(message)
        case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(Status.INVALID_ARGUMENT)
      }
    } yield resp

  def getTopics(request: ScalaGetTopicsRequest): ZIO[MqMockerClient.Service, Status, GetTopicsResponse] =
    for {
      resp <- request.toMessage match {
        case Right(message) => MqMockerClient.getTopics(message)
        case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(Status.INVALID_ARGUMENT)
      }
    } yield resp

  def deleteTopic(request: ScalaDeleteTopicRequest): ZIO[MqMockerClient.Service, Status, DeleteTopicResponse] =
    for {
      resp <- request.toMessage match {
        case Right(message) => MqMockerClient.deleteTopic(message)
        case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(Status.INVALID_ARGUMENT)
      }
    } yield resp
}
