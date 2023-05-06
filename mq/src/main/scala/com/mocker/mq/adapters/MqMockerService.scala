package com.mocker.mq.adapters

import com.mocker.mq.mq_service.ZioMqService.MqMocker
import com.mocker.mq.mq_service._
import com.mocker.mq.ports.MqManager
import io.grpc.Status
import zio.{Cause, Console, IO, ZIO, ZLayer}

case class MqMockerService(brokerManager: MqManager) extends MqMocker {

  override def createTopic(request: CreateTopicRequest): IO[Status, CreateTopicResponse] =
    (for {
      res <- brokerManager.createTopic(request)
    } yield res).tapError(err => ZIO.logErrorCause(Cause.fail(err))).mapError(_.grpcStatus)

  override def sendMessage(request: SendMessageRequest): IO[Status, SendMessageResponse] =
    (for {
      res <- brokerManager.sendMessage(request)
    } yield res).tapError(err => ZIO.logErrorCause(Cause.fail(err))).mapError(_.grpcStatus)

  override def getMessages(request: GetMessagesRequest): IO[Status, GetMessagesResponse] =
    (for {
      res <- brokerManager.getMessages(request)
    } yield res).tapError(err => ZIO.logErrorCause(Cause.fail(err))).mapError(_.grpcStatus)

  override def getTopics(request: GetTopicsRequest): IO[Status, GetTopicsResponse] =
    (for {
      res <- brokerManager.getTopics(request)
    } yield res).tapError(err => ZIO.logErrorCause(Cause.fail(err))).mapError(_.grpcStatus)

  override def deleteTopic(request: DeleteTopicRequest): ZIO[Any, Status, DeleteTopicResponse] =
    (for {
      res <- brokerManager.deleteTopic(request)
    } yield res).tapError(err => ZIO.logErrorCause(Cause.fail(err))).mapError(_.grpcStatus)
}

object MqMockerService {

  def layer: ZLayer[MqManagerImpl, Nothing, MqMockerService] = {
    ZLayer.fromZIO {
      for {
        mqManager <- ZIO.service[MqManager]
      } yield MqMockerService(mqManager)
    }
  }
}
