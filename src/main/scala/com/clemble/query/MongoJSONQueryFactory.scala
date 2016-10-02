package com.clemble.query

import play.api.libs.json.{JsObject, JsNumber}

import com.clemble.query.model._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

class MongoJSONQueryFactory[T](
  collection: JSONCollection,
  queryTranslator: QueryTranslator[JsObject, JsObject] = new MongoJSONQueryTranslator
)(implicit format: Format[T]) extends QueryFactory[T] {

  override def create(exp: Expression): QueryBuilder[T] = {
    new MongoJSONQueryBuilder[T](collection.find(queryTranslator.translate(exp)))
  }

}

private case class MongoJSONQueryBuilder[T](
  var queryBuilder: GenericQueryBuilder[JSONSerializationPack.type]
)(implicit val format: Format[T]) extends QueryBuilder[T] {

  private var pagination: PaginationParams = PaginationParams.empty

  override def pagination(paginationParams: PaginationParams): QueryBuilder[T] = {
    this.pagination = paginationParams
    this
  }

  override def projection(projection: List[Projection]): QueryBuilder[T] = {
    if (projection.isEmpty)
      return this
    val projectionFields = projection.map({
      case Include(field) => field -> JsNumber(1)
      case Exclude(field) => field -> JsNumber(0)
    })
    val projectionQuery = JsObject(projectionFields)
    queryBuilder = queryBuilder.projection(projectionQuery)
    this
  }

  override def sorted(sorts: List[SortOrder]): QueryBuilder[T] = {
    val sortFields = sorts.map({
      case Ascending(field) => field -> JsNumber(1)
      case Descending(field) => field -> JsNumber(-1)
    })
    val sortQuery = JsObject(sortFields)
    queryBuilder = queryBuilder.sort(sortQuery)
    this
  }

  override def findOne()(implicit ec: ExecutionContext): Future[Option[T]] = {
    queryBuilder.one[T]
  }

  override def findOneWithProjection()(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    queryBuilder.one[JsObject].map(_.map(_ - "_id"))
  }

  override def find()(implicit ec: ExecutionContext): Enumerator[T] = {
    queryBuilder.options(QueryOpts(skipN = pagination.offset())).cursor[T]().enumerator(maxDocs = pagination.pageSize)
  }

  override def findWithProjection()(implicit ec: ExecutionContext): Enumerator[JsObject] = {
    queryBuilder.options(QueryOpts(skipN = pagination.offset())).cursor[JsObject]().enumerator(maxDocs = pagination.pageSize).map(_ - "_id")
  }

}
