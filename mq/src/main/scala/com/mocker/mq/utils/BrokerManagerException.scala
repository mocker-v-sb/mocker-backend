package com.mocker.mq.utils

import scala.util.control.NoStackTrace

sealed abstract class BrokerManagerException(msg: String) extends Exception(msg) with NoStackTrace

object BrokerManagerException {
  case class CouldNotCreateTopic(topicName: String, brokerType: BrokerType)
      extends BrokerManagerException(s"Could not create $brokerType/$topicName")

  case class CouldNotSendEvent(topicName: String, brokerType: BrokerType, reason: String = "Unknown error")
      extends BrokerManagerException(s"Could not send event to $brokerType/$topicName due to: $reason")

  case class CouldNotReadFromTopic(topicName: String, brokerType: BrokerType, reason: String = "Unknown error")
      extends BrokerManagerException(s"Could not read events from $brokerType/$topicName due to: $reason")
}
