package com.clemble.query.model

/**
  * Abstraction for pagination params presentation
  *
  * @param page     - page number
  * @param pageSize - page size
  */
case class PaginationParams(page: Int, pageSize: Int) {
  require(page >= 0, s"Page must be greater or equal to 0, got ${page}")
  require(pageSize > 0, s"Page size must be more then 0, got ${pageSize}")
  def offset() = page * pageSize
}
