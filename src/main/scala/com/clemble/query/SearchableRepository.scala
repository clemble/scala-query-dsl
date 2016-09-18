package com.clemble.query

import com.clemble.query.model.{SortOrder, Expression, Query}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.{ExecutionContext, Future}

/**
  * Searchable repository, represents abstract repository that can be searched with provided Query
  */
trait SearchableRepository[T] {

  def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]]

  def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T]

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

}
