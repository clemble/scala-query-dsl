package com.clemble.query.model

/**
  * Simple abstraction for sorting primitives.
  */
sealed trait SortOrder
case class Ascending(field: String) extends SortOrder
case class Descending(field: String) extends SortOrder
