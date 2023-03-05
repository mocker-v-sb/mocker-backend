package com.mocker.mq.adapters

import com.mocker.mq.mq_service.ZioMqService.MqMocker
import com.mocker.mq.mq_service.{CreateTopicRequest, CreateTopicResponse, GetMessagesRequest, GetMessagesResponse, GetTopicsRequest, GetTopicsResponse, SendMessageRequest, SendMessageResponse}
import com.mocker.mq.ports.MqManager
import io.grpc.Status
import zio.{IO, ZIO, ZLayer}

case class MqMockerService(brokerManager: MqManager) extends MqMocker {

  override def createTopic(request: CreateTopicRequest): IO[Status, CreateTopicResponse] =
    (for {
      res <- brokerManager.createTopic(request)
    } yield res).mapBoth(
      error => Status.INTERNAL,
      res => res
    )

  override def sendMessage(request: SendMessageRequest): IO[Status, SendMessageResponse] =
    (for {
      res <- brokerManager.sendMessage(request)
    } yield res).mapBoth(
      error => Status.INTERNAL,
      res => res
    )

  override def getMessages(request: GetMessagesRequest): ZIO[Any, Status, GetMessagesResponse] =
    (for {
      res <- brokerManager.getMessages(request)
    } yield res).mapBoth(
      error => Status.INTERNAL,
      res => res
    )

  override def getTopics(request: GetTopicsRequest): ZIO[Any, Status, GetTopicsResponse] =
}

object MqMockerService {

  def layer: ZLayer[DefaultMqManager, Nothing, MqMockerService] = {
    ZLayer.fromZIO {
      for {
        mqManager <- ZIO.service[MqManager]
      } yield MqMockerService(mqManager)
    }
  }
}
