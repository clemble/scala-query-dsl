package com.clemble.query

import com.clemble.query.model._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Format, JsNumber, JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

trait MongoSearchableRepository[T] extends SearchableRepository[T]{

  val queryTranslator: QueryTranslator[JsObject]
  val collection: JSONCollection
  implicit val f: Format[T]

  override def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]] = {
    val mongoQuery = queryTranslator.translate(query.where)
    collection.find(mongoQuery).one[T]
  }

  override def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T] = {
    val mongoQuery = queryTranslator.translate(query.where)
    collection.find(mongoQuery).cursor[T]().enumerator()
  }

}

/**
  * Default mongo QueryTranslator
  */
class MongoQueryTranslator extends QueryTranslator[JsObject] {

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
      case Empty =>
        Json.obj()
    }
  }

}
