package com.clemble.query

import com.clemble.query.model.{Projection, SortOrder, Expression, Query}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

/**
  * Searchable repository, represents abstract repository that can be searched with provided Query
  */
trait SearchableRepository[T] {

  def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]]

  def findOneWithProjection(query: Query)(implicit ex: ExecutionContext): Future[Option[JsObject]]

  def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T]

  def findWithProjection(query: Query)(implicit ec: ExecutionContext): Enumerator[JsObject]

}

/**
  * Helper class for SearchableRepository, that translates original query to domain specific query
  *
  * @tparam T type of expression result
  * @tparam S type of sort result
  */
trait QueryTranslator[T, S] {

  def translate(where: Expression): T

  def translateSort(sorts: List[SortOrder]): S

  def translateProjection(projection: List[Projection]): S

}
