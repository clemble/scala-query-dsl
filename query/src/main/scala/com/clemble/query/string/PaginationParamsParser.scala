package com.clemble.query.string

import com.clemble.query.core.PaginationParams

case class PaginationParamsParser(
                                   fromParam: String = "page_from",
                                   sizeParam: String = "page_size",
                                   defaultPage: Int = 0,
                                   defaultSize: Int = 25,
                                   maxSize: Int = Int.MaxValue
) {

  /**
    * Query language extension that allows to specify pagination in REST query string.
    * Page numbers start with 0, and if no or negative page number was specified O page is used.
    *
    * For example:
    *  ?page_from=1&page_size=30
    * will return second page with 30 results
    *  ?page_from=0
    * will return first page of default size
    *  ?page_size=50
    * will return first page with 50 records
    *
    * @author Anton Oparin (antono@clemble.com)
    * @param query source query
    * @return tuple with page number & size
    */
  def toPage(query: Map[String, Seq[String]]): PaginationParams = {
    validateParams(query)
    // Step 1. Extract page number
    val pageOpt = query.get(fromParam).flatMap(_.headOption.map(_.toInt))
    val pageSizeOpt = query.get(sizeParam).flatMap(_.headOption.map(_.toInt))
    // Step 2. Extract
    val (page, pageSize) = (pageOpt, pageSizeOpt) match {
      case (Some(page), Some(pageSize)) => page -> pageSize
      case (Some(page), None) => page -> defaultSize
      case (None, Some(pageSize)) => defaultPage -> pageSize
      case (None, None) => defaultPage -> maxSize
    }
    PaginationParams(page, pageSize)
  }

  private def validateParams(query: Map[String, Seq[String]]) = {
    // page & page size can be defined only once
    require(query.get(fromParam).map(_.size).getOrElse(0) <= 1)
    require(query.get(sizeParam).map(_.size).getOrElse(0) <= 1)
  }

}
