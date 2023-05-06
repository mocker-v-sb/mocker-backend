package com.mocker.mq.adapters

import com.mocker.mq.mq_service._
import com.mocker.mq.ports.MqController
import com.mocker.mq.utils.BrokerManagerException
import com.rabbitmq.client.Channel
import zio.IO

case class AmqpController(channel: Channel) extends MqController {
  override def createQueue(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] = ???

  override def deleteTopic(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse] = ???

  override def sendMessages(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] = ???

  override def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse] = ???

  override def getQueues(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] = ???
}
