package com.mocker.mq.adapters

import com.mocker.mq.mq_service.ZioMqService.MqMocker
import com.mocker.mq.mq_service.{CreateTopicRequest, CreateTopicResponse, SendMessageRequest, SendMessageResponse}
import com.mocker.mq.ports.MqManager
import io.grpc.Status
import zio.{IO, ZIO, ZLayer}

case class MqMockerService(mqManager: MqManager) extends MqMocker {

  override def createTopic(request: CreateTopicRequest): IO[Status, CreateTopicResponse] = (for {
    res <- mqManager.createTopic(request)
  } yield res).mapBoth(
    _ => Status.INTERNAL,
    res => res
  )

  override def sendMessage(request: SendMessageRequest): IO[Status, SendMessageResponse] = (for {
    res <- mqManager.sendMessage(request)
  } yield res).mapBoth(
    _ => Status.INTERNAL,
    res => res
  )
}

object MqMockerService {
  def layer: ZLayer[KafkaManager, Nothing, MqMockerService] = {
    ZLayer.fromZIO {
      for {
        mqManager <- ZIO.service[MqManager]
      } yield MqMockerService(mqManager)
    }
  }
}