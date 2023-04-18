package com.mocker.models.mq.responses

import com.mocker.models.mq.Queue
import com.mocker.mq.mq_service.{GetTopicsResponse => ProtoGetTopicsResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetTopicsResponse(queues: Seq[Queue])
object GetTopicsResponse {


  def fromMessage(message: ProtoGetTopicsResponse): Either[String, GetTopicsResponse] = {
    val queuesAndErrors = message.queues.map(q => Queue.fromMessage(q))
    val errors = queuesAndErrors.collect { case Left(error)  => error }
    val queues = queuesAndErrors.collect { case Right(queue) => queue }
    if (errors.isEmpty) Right(GetTopicsResponse(queues))
    else Left(errors.head)
  }

  implicit val encoder = DeriveJsonEncoder.gen[GetTopicsResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetTopicsResponse]
}
