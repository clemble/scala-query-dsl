package com.clemble.query.parser

import com.clemble.query.model.{Empty, Expression, Query}

/**
  * Created by mavarazy on 9/10/16.
  */
case class QueryParser(
                        expressionParser: List[PartialFunction[(String, Seq[String]), Expression]],
                        includeProjection: IncludeProjectionParser,
                        excludeProjection: ExcludeProjectionParser,
                        paginationParser: PaginationParamsParser,
                        sortParser: SortOrderParser
                      ) {

  private val ignore = List(
    sortParser.sortParam,
    paginationParser.fromParam,
    paginationParser.sizeParam,
    includeProjection.includeParam,
    excludeProjection.excludeParam
  )

  private val expression = expressionParser.
    foldRight(PartialFunction.empty[(String, Seq[String]), Expression])
    { (a, b) => a orElse b }

  def parse(query: Map[String, Seq[String]]): Query = {
    val expressionQuery = query.filterKeys(!ignore.contains(_))
    val where = expressionQuery.foldLeft[Expression](Empty)((a, b) => a and expression(b))
    Query(
      where,
      pagination = paginationParser.toPage(query),
      projection = (query.collect(includeProjection).flatten ++ query.collect(excludeProjection).flatten).toList,
      sort = query.collect(sortParser).flatten.toList
    )
  }

}

object QueryParser {

  def DEFAULT = QueryParser(
    List(
      new GreaterThenExpressionParser(),
      new GreaterThenEqualsExpressionParser(),
      new LessThenEqualsExpressionParser(),
      new LessThenExpressionParser(),
      new NotEqualsExpressionParser(),
      EqualsExpressionParser
    ),
    new IncludeProjectionParser(),
    new ExcludeProjectionParser(),
    new PaginationParamsParser(),
    new SortOrderParser()
  )
}
