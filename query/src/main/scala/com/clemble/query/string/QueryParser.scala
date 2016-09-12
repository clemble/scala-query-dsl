package com.clemble.query.string

import com.clemble.query.core._

import scala.collection.immutable.Iterable

/**
  * Created by mavarazy on 9/10/16.
  */
case class QueryParser(
                      expressionParser: List[PartialFunction[(String, Seq[String]), Expression]],
                      projectionParser: ProjectionParser,
                      paginationParser: PaginationParamsParser,
                      sortParser: SortOrderParser
                      ) {

  private val ignore = List(
    sortParser.sortParam,
    paginationParser.fromParam,
    paginationParser.sizeParam,
    projectionParser.excludeParam,
    projectionParser.includeParam
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
      projection = query.collect(projectionParser).flatten.toList,
      sort = query.collect(sortParser).flatten.toList
    )
  }

}
