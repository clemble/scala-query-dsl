package com.clemble.query.core

import com.clemble.query.core.model._
import play.api.libs.json.{JsNumber, JsObject, Json}

/**
  * Created by mavarazy on 9/17/16.
  */
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
