package com.mocker.rest.avro

import com.mocker.rest.avro.AvroSchemaError._
import io.circe.{Json, JsonObject}
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.avro.Schema.Field
import scala.jdk.CollectionConverters._

import scala.util.matching.Regex

/**
 * Credits to https://github.com/irajhedayati/savro
 */
object AvroSchema {

  import AvroImplicits._

  val FloatingPointPattern: Regex = "[-+]?[0-9]*\\.[0-9]+".r

  def mergeRecordSchema(left: Schema, right: Schema): Schema = {
    val leftKeyed: Seq[(String, Field)] = left.getFields.asScala.toSeq.map(f => f.name() -> f)
    val rightKeyed: Seq[(String, Field)] = right.getFields.asScala.toSeq.map(f => f.name() -> f)
    val builder = SchemaBuilder.record(left.getName).namespace(left.getNamespace).fields()
    val x: Map[String, Seq[(String, Field)]] = (leftKeyed ++ rightKeyed).groupBy(_._1)
    val y: Seq[(String, List[Field])] = x.map(a => (a._1, a._2.map(_._2).toList)).toSeq.sortBy(_._1)
    y.foldLeft(builder) {
      case (b, (fieldName, field :: Nil)) =>
        b.name(fieldName).`type`(field.schema()).withDefault(null)
      case (b, (fieldName, existingField :: newField :: Nil)) if newField.hasSameSchema(existingField) =>
        b.name(fieldName).`type`(existingField.schema()).withDefault(null)
      case (b, (fieldName, existingField :: newField :: Nil))
          if existingField.schema().getTypesWithoutNull.isRecord && newField.schema().getTypesWithoutNull.isRecord =>
        b.name(fieldName)
          .`type`(
            mergeRecordSchema(
              existingField.schema().getTypesWithoutNull,
              newField.schema().getTypesWithoutNull
            ).makeNullable
          )
          .withDefault(null)
      case (b, (fieldName, existingField :: newField :: Nil)) if existingField.schema().getTypesWithoutNull.isArray =>
        val itemSchema = existingField.schema().mergeWith(newField.schema()).makeNullable
        b.name(fieldName).`type`(itemSchema).withDefault(null)
      case (b, (fieldName, existingField :: newField :: Nil)) =>
        b.name(fieldName)
          .`type`(existingField.schema().getTypesWithoutNull.union(newField.schema().getTypesWithoutNull).makeNullable)
          .withDefault(null)
    }
    builder.endRecord()
  }

  def inferRecord(json: Json, name: String, nameSpace: Option[String]): Either[IllegalJsonInput, Schema] =
    json.asObject match {
      case Some(value) => inferRecord(value, name, nameSpace)
      case None        => Left(IllegalJsonInput(json, "Unable to parse input to a JSON object"))
    }

  def inferRecord(
      jsonObject: JsonObject,
      name: String,
      nameSpace: Option[String]
  ): Either[IllegalJsonInput, Schema] = {
    val recordName = s"${name.head.toUpper.toString}${name.tail}"
    val builder = SchemaBuilder.record(recordName).namespace(nameSpace.getOrElse("")).fields()
    jsonObject.toList
      .filter(_._1.nonEmpty)
      .map {
        case (fieldName, fieldValue) =>
          val sanitizedFieldName = SanitizedFieldName(fieldName)
          (sanitizedFieldName.validFieldName, fieldValue, sanitizedFieldName.explanation.orNull)
      }
      .sortBy(_._1)
      .foreach {
        case (fieldName, fieldValue, doc) =>
          inferSchema(fieldValue, fieldName, nameSpace, isArrayItem = false) match {
            case Left(error)        => return Left(error)
            case Right(fieldSchema) => builder.name(fieldName).doc(doc).`type`(fieldSchema).withDefault(null)
          }
      }
    Right(builder.endRecord())
  }

  def inferSchema(
      json: Json,
      name: String,
      namespace: Option[String],
      isArrayItem: Boolean
  ): Either[IllegalJsonInput, Schema] =
    json match {
      case _ if json.isNull    => Right(SchemaBuilder.builder().nullType())
      case _ if json.isString  => Right(SchemaBuilder.builder().stringType().makeNullable)
      case _ if json.isBoolean => Right(SchemaBuilder.builder().booleanType().makeNullable)
      case _ if json.isNumber && FloatingPointPattern.findFirstIn(json.toString()).nonEmpty =>
        Right(SchemaBuilder.builder().doubleType().makeNullable)
      case _ if json.isNumber && json.asNumber.fold(false)(_.toInt.isDefined) =>
        Right(SchemaBuilder.builder().intType().makeNullable)
      case _ if json.isNumber =>
        Right(SchemaBuilder.builder().longType().makeNullable)
      case _ if json.isObject && isArrayItem && name.endsWith("s") =>
        inferRecord(json, name.init, namespace).map(_.makeNullable)
      case _ if json.isObject => inferRecord(json, name, namespace).map(_.makeNullable)
      case _ if json.isArray  => inferArray(json, name, namespace).map(_.makeNullable)
    }

  def inferArray(json: Json, name: String, namespace: Option[String]): Either[IllegalJsonInput, Schema] =
    json.asArray match {
      case Some(value) =>
        value.headOption match {
          case Some(head) => inferSchema(head, name, namespace, isArrayItem = true).map(SchemaBuilder.array().items(_))
          case None       => Right(SchemaBuilder.array().items(SchemaBuilder.builder().nullType()))
        }
      case None => Left(IllegalJsonInput(json, "Unable to parse input to a JSON array"))
    }

}
