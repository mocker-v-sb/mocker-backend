package com.mocker.mq.ports

import com.mocker.mq.mq_service._
import zio.Task

trait MqManager {

  def createTopic(request: CreateTopicRequest): Task[CreateTopicResponse]

  def sendMessage(request: SendMessageRequest): Task[SendMessageResponse]
}
