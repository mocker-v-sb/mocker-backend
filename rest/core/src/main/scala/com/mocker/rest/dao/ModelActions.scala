package com.mocker.rest.dao

import com.mocker.rest.model.Model
import slick.dbio.DBIO

trait ModelActions {

  def upsert(model: Model): DBIO[Unit]
}
