package com.mocker.models.mq.requests

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.mq.mq_service.{BrokerType, GetTopicsRequest => ProtoGetTopicsRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetTopicsRequest(brokerType: String) {

  def toMessage: Either[String, ProtoGetTopicsRequest] = {
    ScalaBrokerType.getBrokerType(brokerType) match {
      case Right(bt) =>
        bt match {
          case BrokerType.BROKER_TYPE_KAFKA               => Right(ProtoGetTopicsRequest(brokerType = bt))
          case BrokerType.BROKER_TYPE_RABBITMQ            => Left("RabbitMQ support is not implemented yet.")
          case BrokerType.Unrecognized(unrecognizedValue) => Left(s"Broker type $unrecognizedValue not recognized")
          case _                                          => Left("Broker type not defined")
        }
      case Left(error) => Left(error)
    }
  }
}

object GetTopicsRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetTopicsRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetTopicsRequest]
}
