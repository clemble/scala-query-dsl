package com.clemble.query

import com.clemble.query.model._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

trait MongoSearchableRepository[T] extends SearchableRepository[T]{

  val queryTranslator: QueryTranslator[JsObject]
  val collection: JSONCollection
  implicit val f: Format[T]

  override def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.one[T]
  }

  override def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.cursor[T]().enumerator(maxDocs = query.pagination.pageSize)
  }

  private def buildQuery(query: Query)(implicit ec: ExecutionContext): GenericQueryBuilder[collection.pack.type]#Self = {
    val mongoQuery = queryTranslator.translate(query.where)
    val sortQuery = queryTranslator.translateSort(query.sort)
    collection.find(mongoQuery).sort(sortQuery).options(QueryOpts(skipN = query.pagination.offset()))
  }

  private def toSort(query: Query): JsObject = {
    val sortFields = query.sort.map({
      case Ascending(field) => field -> JsNumber(1)
      case Descending(field) => field -> JsNumber(-1)
    })
    JsObject(sortFields)
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
        Json.obj(field -> Json.obj("$ne" -> value))
      case Equals(field, value) =>
        Json.obj(field -> value)
      case LessThen(field, value) =>
        Json.obj(field -> Json.obj("$lt" ->JsNumber(value)))
      case LessThenEquals(field, value) =>
        Json.obj(field -> Json.obj("$lte" -> JsNumber(value)))
      case GreaterThen(field, value) =>
        Json.obj(field -> Json.obj("$gt" -> JsNumber(value)))
      case GreaterThenEquals(field, value) =>
        Json.obj(field -> Json.obj("$gte" -> JsNumber(value)))
      case Empty =>
        Json.obj()
    }
  }

  override def translateSort(sorts: List[SortOrder]): JsObject = {
    val sortFields = sorts.map({
      case Ascending(field) => field -> JsNumber(1)
      case Descending(field) => field -> JsNumber(-1)
    })
    JsObject(sortFields)
  }

}
