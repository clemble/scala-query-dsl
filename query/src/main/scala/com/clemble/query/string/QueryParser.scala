package com.clemble.query.string

import com.clemble.query.core._

import scala.collection.immutable.Iterable

/**
  * Created by mavarazy on 9/10/16.
  */
case class QueryParser(
                      expressionParser: List[ExpressionParser],
                      projectionParser: ProjectionParser,
                      paginationParser: PaginationParamsParser,
                      sortParser: SortOrderParser
                      ) {

  val ignore = List(
    sortParser.sortParam,
    paginationParser.fromParam,
    paginationParser.sizeParam,
    projectionParser.excludeParam,
    projectionParser.includeParam
  )

  def parse(query: Map[String, Seq[String]]): Query = {
    val expressionQuery = query.filterKeys(!ignore.contains(_))
    val expressions: Iterable[Option[Expression]] = for {
      query <- expressionQuery
    } yield {
      expressionParser.find(_.isDefinedAt(query)).map(_(query))
    }
    val where = expressions.foldLeft[Expression](Empty)((a, b) => a and(b.get))
    Query(
      where,
      pagination = paginationParser.toPage(query),
      projection = projectionParser.toProjection(query),
      sort = sortParser.toSort(query)
    )
  }

}
