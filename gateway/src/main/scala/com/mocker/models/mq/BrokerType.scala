package com.mocker.models.mq

import com.mocker.mq.mq_service.BrokerType
import com.mocker.mq.mq_service.BrokerType.BROKER_TYPE_KAFKA

object BrokerType {
  val KAFKA = "KAFKA"
  val RABBITMQ = "RABBITMQ"

  def getBrokerType(brokerType: String): Either[String, BrokerType] = {
    if (brokerType.equals(KAFKA)) Right(BROKER_TYPE_KAFKA)
    else if (brokerType.equals(RABBITMQ)) Left("Rabbit MQ is not supported yet")
    else Left("Broker not defined")
  }
}
