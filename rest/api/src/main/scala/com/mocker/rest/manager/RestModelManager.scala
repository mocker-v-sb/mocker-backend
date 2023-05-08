package com.mocker.rest.manager

import com.mocker.rest.dao.ModelActions
import com.mocker.rest.dao.mysql.MySqlModelActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Model, Service}
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

case class RestModelManager(
    restMockerDbProvider: DatabaseProvider,
    serviceManager: RestServiceManager,
    mockManager: RestMockManager,
    modelActions: ModelActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def upsertModel(servicePath: String, model: Model): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      _ <- modelActions.upsert(model.copy(serviceId = service.id)).asZIO(dbLayer).run
    } yield ()
  }

  def getModel(servicePath: String, modelId: Long): IO[RestMockerException, Model] = {
    for {
      service <- serviceManager.getService(servicePath)
      model <- checkModelExists(service, modelId)
    } yield model
  }

  def getModel(modelId: Long): IO[RestMockerException, Option[Model]] = {
    modelActions.get(modelId).asZIO(dbLayer).run
  }

  def getAllServiceModels(servicePath: String): IO[RestMockerException, Seq[Model]] = {
    for {
      service <- serviceManager.getService(servicePath)
      models <- modelActions.getAll(service.id).asZIO(dbLayer).run
    } yield models
  }

  def deleteModel(servicePath: String, modelId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      existingMocks <- mockManager.findByModel(service.id, modelId)
      _ <- if (existingMocks.nonEmpty)
        ZIO.fail(RestMockerException.modelInUse(servicePath, existingMocks))
      else
        modelActions.delete(service.id, modelId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllModels(servicePath: String): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      models <- modelActions.getAll(service.id).asZIO(dbLayer).run
      mocks <- mockManager.findByModels(service.id, models.map(_.id))
      _ <- mocks.toList match {
        case Nil => modelActions.deleteAll(service.id).asZIO(dbLayer).run
        case _   => ZIO.fail(RestMockerException.modelInUse(servicePath, mocks))
      }
    } yield ()
  }

  private def checkModelExists(service: Service, modelId: Long): IO[RestMockerException, Model] = {
    for {
      dbModel <- modelActions.get(serviceId = service.id, modelId = modelId).asZIO(dbLayer).run
      result <- dbModel match {
        case Some(model) => ZIO.succeed(model)
        case None        => ZIO.fail(RestMockerException.modelNotExists(servicePath = service.path, modelId = modelId))
      }
    } yield result
  }
}

object RestModelManager {

  def layer(
      implicit ec: ExecutionContext
  ): URLayer[DatabaseProvider with RestServiceManager with RestMockManager, RestModelManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
        serviceManager <- ZIO.service[RestServiceManager]
        mockManager <- ZIO.service[RestMockManager]
      } yield RestModelManager(
        restMockerDatabase,
        serviceManager,
        mockManager,
        MySqlModelActions()
      )
    }
  }

}
