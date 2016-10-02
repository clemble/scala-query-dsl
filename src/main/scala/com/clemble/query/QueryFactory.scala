package com.clemble.query

import com.clemble.query.model.{PaginationParams, Expression, SortOrder, Projection}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

/**
  * Query factory
  */
trait QueryFactory[T] {

  def create(exp: Expression): QueryBuilder[T]

}

trait QueryBuilder[T] {

  def pagination(pagination: PaginationParams): QueryBuilder[T]

  def projection(projection: List[Projection]): QueryBuilder[T]

  def sorted(sort: List[SortOrder]): QueryBuilder[T]

  def findOne()(implicit ec: ExecutionContext): Future[Option[T]]

  def findOneWithProjection()(implicit ec: ExecutionContext): Future[Option[JsObject]]

  def find()(implicit ec: ExecutionContext): Enumerator[T]

  def findWithProjection()(implicit ec: ExecutionContext): Enumerator[JsObject]

}
