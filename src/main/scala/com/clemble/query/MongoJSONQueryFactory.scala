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

  override def create(): QueryBuilder[T] = {
    new MongoJSONQueryBuilder[T](collection.genericQueryBuilder, queryTranslator)
  }

}

private case class MongoJSONQueryBuilder[T](
  private val queryBuilder: GenericQueryBuilder[JSONSerializationPack.type],
  queryTranslator: QueryTranslator[JsObject, JsObject]
)(implicit val format: Format[T]) extends QueryBuilder[T] {

  private var pagination: PaginationParams = PaginationParams.empty

  override def where(exp: Expression): QueryBuilder[T] = {
    val mongoQuery = queryTranslator.translate(exp)
    queryBuilder.query(mongoQuery)
    this
  }

  override def pagination(paginationParams: PaginationParams): QueryBuilder[T] = {
    this.pagination = paginationParams
    this
  }

  override def withProjection(projection: List[Projection]): QueryBuilder[T] = {
    val projectionFields = projection.map({
      case Include(field) => field -> JsNumber(1)
      case Exclude(field) => field -> JsNumber(0)
    })
    val projectionQuery = JsObject(projectionFields)
    queryBuilder.projection(projectionQuery)
    this
  }

  override def sorted(sorts: List[SortOrder]): QueryBuilder[T] = {
    val sortFields = sorts.map({
      case Ascending(field) => field -> JsNumber(1)
      case Descending(field) => field -> JsNumber(-1)
    })
    val sortQuery = JsObject(sortFields)
    queryBuilder.sort(sortQuery)
    this
  }

  override def findOne()(implicit ec: ExecutionContext): Future[Option[T]] = {
    queryBuilder.one[T]
  }

  override def findOneWithProjection()(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    queryBuilder.one[JsObject]
  }

  override def find()(implicit ec: ExecutionContext): Enumerator[T] = {
    queryBuilder.options(QueryOpts(skipN = pagination.offset())).cursor[T]().enumerator(maxDocs = pagination.pageSize)
  }

  override def findWithProjection()(implicit ec: ExecutionContext): Enumerator[JsObject] = {
    queryBuilder.options(QueryOpts(skipN = pagination.offset())).cursor[JsObject]().enumerator(maxDocs = pagination.pageSize).map(_ - "_id")
  }

}
