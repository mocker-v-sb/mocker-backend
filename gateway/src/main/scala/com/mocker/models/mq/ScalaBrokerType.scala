package com.mocker.models.mq

import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType}
import com.mocker.mq.mq_service.BrokerType.{BROKER_TYPE_KAFKA, BROKER_TYPE_UNDEFINED}

object ScalaBrokerType {
  val KAFKA = "KAFKA"
  val RABBITMQ = "RABBITMQ"
  val ANY = "ANY"

  def getBrokerType(brokerType: String): Either[String, ProtoBrokerType] = {
    if (brokerType.equals(KAFKA)) Right(BROKER_TYPE_KAFKA)
    else if (brokerType.equals(RABBITMQ)) Left("Rabbit MQ is not supported yet")
    else if (brokerType.equals(ANY)) Right(BROKER_TYPE_UNDEFINED)
    else Left("Broker not defined")
  }

  def fromMessage(message: ProtoBrokerType): Either[String, String] = {
    message match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA               => Right(KAFKA)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ            => Right(RABBITMQ)
      case ProtoBrokerType.Unrecognized(unrecognizedValue) => Left(s"Unrecognized broker type: $unrecognizedValue")
      case _                                               => Right("Undefined")
    }
  }
}
