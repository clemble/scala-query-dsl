package com.clemble.query.string

import com.clemble.query.core.{Descending, Ascending, SortOrder}

/**
  * Allows to change sorting order by providing query params in REST request.
  * It supports multiple sorting, for the collection
  *
  * For example:
  *    ?sort=name&sort=model
  *
  * will be translated to sort by name & order in default ASC order.
  *
  * Default sorting order is ASC, in order to change it to DESC add exclamation mark at the beginning of the param.
  *
  * For example:
  *    ?sort=name&sort=!model
  *
  * will be translated to sort ASC by name and DESC by model.
  *
  * Extension allows to specify query parameters in different formats
  *
  *   ?sort=name,model
  *   Is the same as
  *   ?sort=name&sort=model
  *
  *   or
  *
  *   ?sort=name,model,!year
  *   Is the same as
  *   ?sort=name&sort=model&sort=!year
  *
  * @return sort JsObject interpretable by mongo
  */
case class SortOrderParser (sortParam: String = "sort"){

  def toSort(sortQueryParams: Map[String, Seq[String]]): List[SortOrder] = {
    // Step 1. Filter sort key
    sortQueryParams.get(sortParam) match {
      case Some(columns) =>
        val fieldSort = columns.
          flatMap(_.split(",")).
          map(_.trim()).
          map(toSortDirection)
        // Step 2. Returning generated field sort query
        fieldSort.toList
      case None =>
        // Case 2. No explicit ordering specified
        List.empty[SortOrder]
    }
  }

  private def toSortDirection(column: String): SortOrder = {
    if (column.startsWith("!") ) {
      Descending(column.substring(1))
    } else {
      Ascending(column)
    }
  }

}
