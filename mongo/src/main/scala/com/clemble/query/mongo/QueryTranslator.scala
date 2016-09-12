package com.clemble.query.mongo

import com.clemble.query.core._
import play.api.libs.json.{JsNumber, Json, JsObject}

trait QueryTranslator {

  def translate(query: Expression): JsObject

}

class MongoQueryTranslator extends QueryTranslator {

  override def translate(query: Expression): JsObject = {
    query match {
      case And(conditions) =>
        JsObject(conditions.map(translate).flatMap(obj => obj.fields))
      case Or(conditions) =>
        Json.obj("$or" -> conditions.map(translate))
      case NotEquals(field, value) =>
        Json.obj("$ne" -> Json.obj(field -> value))
      case Equals(field, value) =>
        Json.obj(field -> value)
      case LessThen(field, value) =>
        Json.obj("$lt" -> Json.obj(field -> JsNumber(value)))
      case LessThenEquals(field, value) =>
        Json.obj("$lte" -> Json.obj(field -> JsNumber(value)))
      case GreaterThen(field, value) =>
        Json.obj("$gt" -> Json.obj(field -> JsNumber(value)))
      case GreaterThenEquals(field, value) =>
        Json.obj("$gte" -> Json.obj(field -> JsNumber(value)))
    }
  }

}