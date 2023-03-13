package com.mocker.mq.utils

sealed trait BrokerType

object BrokerType {

  case object Kafka extends BrokerType

  case object RabbitMQ extends BrokerType

  case object Unknown extends BrokerType
}
