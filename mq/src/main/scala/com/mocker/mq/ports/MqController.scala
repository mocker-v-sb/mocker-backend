package com.mocker.mq.ports

import com.mocker.mq.mq_service._
import com.mocker.mq.utils.BrokerManagerException
import zio.IO

trait MqController {
  def createQueue(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse]

  def getQueues(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse]

  def deleteQueue(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse]

  def sendMessages(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse]

  def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse]
}
