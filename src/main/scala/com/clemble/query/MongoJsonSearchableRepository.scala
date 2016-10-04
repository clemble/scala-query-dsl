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

trait MongoJSONSearchableRepository[T] extends SearchableRepository[T]{

  val queryTranslator: QueryTranslator[JsObject, JsObject]
  val collection: JSONCollection
  implicit val f: Reads[T]

  override def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.one[T]
  }

  override def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.cursor[T]().enumerator(maxDocs = query.pagination.pageSize)
  }

  protected def buildQuery(query: Query)(implicit ec: ExecutionContext): GenericQueryBuilder[collection.pack.type]#Self = {
    val mongoQuery = queryTranslator.translate(query.where)
    val sortQuery = queryTranslator.translateSort(query.sort)
    collection.find(mongoQuery).sort(sortQuery).options(QueryOpts(skipN = query.pagination.offset()))
  }

}

trait MongoJSONProjectionSupport[T] extends MongoJSONSearchableRepository[T] with ProjectionSupport {

  override def findOneWithProjection(query: Query)(implicit ex: ExecutionContext): Future[Option[JsObject]] = {
    val mongoQuery = buildQuery(query)
    val mongoQueryWithProjection = specifyProjection(mongoQuery, query.projection)
    mongoQueryWithProjection.one[JsObject].map(_.map(_ - "_id"))
  }

  override def findWithProjection(query: Query)(implicit ec: ExecutionContext): Enumerator[JsObject] = {
    val mongoQuery = buildQuery(query)
    val mongoQueryWithProjection = specifyProjection(mongoQuery, query.projection)
    mongoQueryWithProjection.cursor[JsObject]().enumerator(maxDocs = query.pagination.pageSize).map(_ - "_id")
  }

  private def specifyProjection(query: GenericQueryBuilder[collection.pack.type]#Self, projection: List[Projection]) = {
    val projectionFields = projection.map({
      case Include(field) => field -> JsNumber(1)
      case Exclude(field) => field -> JsNumber(0)
    })
    query.projection(JsObject(projectionFields))
  }

}

/**
  * Default mongo QueryTranslator
  */
class MongoJSONQueryTranslator extends QueryTranslator[JsObject, JsObject] {

  override def translate(query: Expression): JsObject = {
    query match {
      case And(conditions) =>
        Json.obj("$and" -> conditions.map(translate))
      case Or(conditions) =>
        Json.obj("$or" -> conditions.map(translate))
      case NotEquals(field, value) =>
        Json.obj(field -> Json.obj("$ne" -> value))
      case Equals(field, value) =>
        Json.obj(field -> value)
      case IntEquals(field, value) =>
        Json.obj(field -> value)
      case IntNotEquals(field, value) =>
        Json.obj(field -> Json.obj("$ne" -> value))
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
