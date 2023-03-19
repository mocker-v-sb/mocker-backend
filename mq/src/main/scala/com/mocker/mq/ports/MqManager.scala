package com.mocker.mq.ports

import com.mocker.mq.mq_service._
import com.mocker.mq.utils.BrokerManagerException
import zio.IO

trait MqManager {

  def createTopic(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse]

  def sendMessage(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse]

  def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse]

  def getTopics(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse]

  def deleteTopic(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse]
}
