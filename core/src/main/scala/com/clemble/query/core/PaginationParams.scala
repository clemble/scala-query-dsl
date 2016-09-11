package com.clemble.query.core

/**
  * Abstraction for pagination params presentation
  *
  * @param page     - page number
  * @param pageSize - page size
  */
case class PaginationParams(page: Int, pageSize: Int) {
  def isValid() = page >= 0 && pageSize > 0
  def offset() = page * pageSize
}
