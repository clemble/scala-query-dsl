package com.clemble.query

import reactivemongo.api.{BSONSerializationPack}
import reactivemongo.bson.{BSONDocumentReader, BSONInteger, BSONDocument}

import reactivemongo.play.json.ImplicitBSONHandlers._

import com.clemble.query.model._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject}
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import reactivemongo.play.iteratees.cursorProducer
import scala.concurrent.{Future, ExecutionContext}

class MongoBSONQueryFactory[T](
                                collection: BSONCollection,
                                queryTranslator: QueryTranslator[BSONDocument, BSONDocument] = new MongoBSONQueryTranslator
                              )(implicit format: BSONDocumentReader[T]) extends QueryFactory[T] {

  override def create(exp: Expression): QueryBuilder[T] = {
    new MongoBSONQueryBuilder[T](collection.find(queryTranslator.translate(exp)))
  }

}

private case class MongoBSONQueryBuilder[T](
                                             var queryBuilder: GenericQueryBuilder[BSONSerializationPack.type]
                                           )(implicit val format: BSONDocumentReader[T]) extends QueryBuilder[T] {

  private var pagination: PaginationParams = PaginationParams.empty

  override def pagination(paginationParams: PaginationParams): QueryBuilder[T] = {
    this.pagination = paginationParams
    this
  }

  override def projection(projection: List[Projection]): QueryBuilder[T] = {
    if (projection.isEmpty)
      return this
    val projectionFields = projection.map({
      case Include(field) => field -> BSONInteger(1)
      case Exclude(field) => field -> BSONInteger(0)
    })
    val projectionQuery = BSONDocument(projectionFields)
    queryBuilder = queryBuilder.projection(projectionQuery)
    this
  }

  override def sorted(sorts: List[SortOrder]): QueryBuilder[T] = {
    val sortFields = sorts.map({
      case Ascending(field) => field -> BSONInteger(1)
      case Descending(field) => field -> BSONInteger(-1)
    })
    val sortQuery = BSONDocument(sortFields)
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

