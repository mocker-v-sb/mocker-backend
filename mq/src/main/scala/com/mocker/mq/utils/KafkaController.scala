package com.mocker.mq.utils

import com.mocker.common.utils.ServerAddress
import zio.kafka.admin.AdminClient
import zio.kafka.producer.Producer

case class KafkaController(adminClient: AdminClient, producer: Producer, address: ServerAddress)

case class RabbitMqController()
