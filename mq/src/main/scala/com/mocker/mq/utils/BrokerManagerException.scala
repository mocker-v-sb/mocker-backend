package com.mocker.mq.utils

import io.grpc.Status

import scala.util.control.NoStackTrace

case class BrokerManagerException(msg: String, grpcStatus: Status) extends Exception(msg) with NoStackTrace

object BrokerManagerException {

  def couldNotCreateTopic(
      topicName: String,
      brokerType: BrokerType,
      reason: String,
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException(s"Could not create $brokerType/$topicName due to: $reason", grpcStatus)

  def couldNotSendEvent(
      topicName: String,
      brokerType: BrokerType,
      reason: String = "Unknown error",
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException(s"Could not send event to $brokerType/$topicName due to: $reason", grpcStatus)

  def couldNotReadFromTopic(
      topicName: String,
      brokerType: BrokerType,
      reason: String = "Unknown error",
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException(s"Could not read events from $brokerType/$topicName due to: $reason", grpcStatus)

  def couldNotGetTopicsList(
      brokerType: BrokerType,
      reason: String = "Unknown error",
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException(s"Could not get $brokerType topics list due to: $reason", grpcStatus)

  def couldNotDeleteTopic(
      topicName: String,
      brokerType: BrokerType,
      reason: String = "Unknown error",
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException(s"Could not delete $brokerType topic $topicName due to: $reason", grpcStatus)
}
