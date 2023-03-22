package com.mocker.rest.avro

import com.mocker.rest.avro.AvroSchema.mergeRecordSchema
import org.apache.avro.Schema.Field
import org.apache.avro.Schema.Type._
import org.apache.avro.{JsonProperties, Schema, SchemaBuilder}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

/**
  * Credits to https://github.com/irajhedayati/savro
  */
object AvroImplicits {

  implicit class SchemaFieldOps(field: Field) {

    def hasSameSchema(other: Field): Boolean =
      field.schema().getTypesWithoutNull.equals(other.schema().getTypesWithoutNull)

    def default: AnyRef = {
      val originalDefault = field.defaultVal()
      if (originalDefault.isInstanceOf[JsonProperties.Null]) null
      else originalDefault
    }
  }

  implicit class SchemaImprovement(schema: Schema) {

    final def getTypesWithoutNull: Schema = schema match {
      case _ if schema.isNullable && schema.isUnion => // It could be just NULL type
        schema.getTypes.asScala.toList.filter(_.getType != NULL) match {
          case singleType :: Nil => singleType
          case multipleTypes     => Schema.createUnion(multipleTypes.asJava)
        }
      case _ => schema
    }

    final def mergeWith(otherSchema: Schema): Schema = (schema, otherSchema) match {
      case (_, _) if schema.equals(otherSchema)              => schema
      case (_, _) if schema.isRecord && otherSchema.isRecord => mergeRecordSchema(schema, otherSchema)
      case (_, _)
          if schema.isRecord && otherSchema.isUnion &&
            otherSchema.getTypes.asScala.exists(recordMatcher(_)(schema.getFullName)) =>
        val matchingRecord = otherSchema.getTypes.asScala.find(recordMatcher(_)(schema.getFullName)).get
        val unionWithOutMatchingRecord =
          Schema.createUnion(otherSchema.getTypes.asScala.filter(!recordMatcher(_)(schema.getFullName)).asJava)
        schema.mergeWith(matchingRecord).unionWithUnion(unionWithOutMatchingRecord)
      case (_, _)
          if otherSchema.isRecord && schema.isUnion &&
            schema.getTypes.asScala.exists(recordMatcher(_)(otherSchema.getFullName)) =>
        val matchingRecord = schema.getTypes.asScala.find(recordMatcher(_)(otherSchema.getFullName)).get
        val unionWithOutMatchingRecord =
          Schema.createUnion(schema.getTypes.asScala.filter(!recordMatcher(_)(otherSchema.getFullName)).asJava)
        otherSchema.mergeWith(matchingRecord).unionWithUnion(unionWithOutMatchingRecord)
      case (_, _) if schema.isUnion && schema.isNullable && otherSchema.isUnion && otherSchema.isNullable =>
        schema.getTypesWithoutNull.mergeWith(otherSchema.getTypesWithoutNull).makeNullable
      case (_, _) if !schema.isUnion && schema.isNullable && otherSchema.isUnion && otherSchema.isNullable =>
        otherSchema
      case (_, _) if schema.isUnion && schema.isNullable && !otherSchema.isUnion && otherSchema.isNullable => schema
      case (_, _) if schema.isArray && !otherSchema.isArray && !otherSchema.isUnion && !otherSchema.isNullable =>
        SchemaBuilder.unionOf().`type`(schema).and().`type`(otherSchema).endUnion()
      case (_, _) if otherSchema.isArray && !schema.isArray && !schema.isUnion && !schema.isNullable =>
        SchemaBuilder.unionOf().`type`(schema).and().`type`(otherSchema).endUnion()
      case (_, _) if schema.isArray && !otherSchema.isArray && !otherSchema.isUnion && otherSchema.isNullable =>
        schema.makeNullable
      case (_, _) if otherSchema.isArray && !schema.isArray && !schema.isUnion && schema.isNullable =>
        otherSchema.makeNullable
      case (_, _) if schema.isArray && !otherSchema.isArray && otherSchema.isUnion && otherSchema.isNullable =>
        schema.mergeWith(otherSchema.getTypesWithoutNull).makeNullable
      case (_, _) if otherSchema.isArray && !schema.isArray && schema.isUnion && schema.isNullable =>
        schema.getTypesWithoutNull.mergeWith(otherSchema).makeNullable
      case (_, _) if schema.isArray && !otherSchema.isArray && otherSchema.isUnion && !otherSchema.isNullable =>
        unionWithUnion(otherSchema)
      case (_, _) if otherSchema.isArray && !schema.isArray && schema.isUnion && !schema.isNullable =>
        unionWithNonUnion(otherSchema)
      case (_, _) if schema.isArray && otherSchema.isArray =>
        SchemaBuilder.array().items(schema.getElementType.mergeWith(otherSchema.getElementType).makeNullable)
    }

    final def makeNullable: Schema =
      if (schema.isNullable) schema
      else if (schema.isUnion) SchemaBuilder.builder().nullType().unionWithUnion(schema)
      else SchemaBuilder.builder().unionOf().nullType().and().`type`(schema).endUnion()

    final def isRecord: Boolean = schema.getType.equals(Schema.Type.RECORD)
    final def isArray: Boolean = schema.getType.equals(Schema.Type.ARRAY)

    @tailrec
    final def union(other: Schema): Schema =
      if (schema.getType.equals(NULL) && other.getType.equals(NULL)) schema
      else if (other.getType.equals(NULL)) other.union(schema)
      else if (!schema.isUnion && !other.isUnion) SchemaBuilder.unionOf().`type`(schema).and().`type`(other).endUnion()
      else if (schema.isUnion && !other.isUnion) unionWithNonUnion(other)
      else if (!schema.isUnion && other.isUnion) other.unionWithNonUnion(schema)
      else Schema.createUnion((schema.getTypes.asScala ++ other.getTypes.asScala).distinct.asJava)

    def unionWithNonUnion(nonUnion: Schema): Schema = {
      val temp = schema.getTypes.asScala.filter(_.isRecord).filter(_.getFullName.equals(nonUnion.getFullName)).toList
      val nonUnionAfterMerge = if (temp.isEmpty) nonUnion else mergeRecordSchema(temp.head, nonUnion)
      val types = schema.getTypes.asScala
        .filterNot(t => t.isRecord && t.getFullName.equals(nonUnion.getFullName)) :+ nonUnionAfterMerge
      Schema.createUnion(types.distinct.asJava)
    }

    def unionWithUnion(union: Schema): Schema = {
      val temp = union.getTypes.asScala.filter(_.isRecord).filter(_.getFullName.equals(schema.getFullName)).toList
      val nonUnionAfterMerge = if (temp.isEmpty) schema else mergeRecordSchema(temp.head, schema)
      val types = Seq(nonUnionAfterMerge) ++ union.getTypes.asScala
        .filterNot(t => t.isRecord && t.getFullName.equals(schema.getFullName))
      Schema.createUnion(types.asJava)
    }

    private def recordMatcher(schema: Schema)(fullName: String): Boolean =
      schema.isRecord && schema.getFullName.equals(fullName)

  }
}
