package com.clemble.query.core.model

/**
  * This is query parameters for find requests
  *
  * @param where      query in string to seq string parameters
  * @param pagination pagination parameters
  * @param projection projection to use
  * @param sort       sort order
  */
case class Query(
                  where: Expression,
                  pagination: PaginationParams = PaginationParams(0, Int.MaxValue),
                  projection: List[Projection] = List.empty[Projection],
                  sort: List[SortOrder] = List.empty[SortOrder]
)