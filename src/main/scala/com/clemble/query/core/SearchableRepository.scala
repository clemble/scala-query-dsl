package com.clemble.query.core

import com.clemble.query.core.model.Query
import play.api.libs.iteratee.Enumerator

/**
  * Searchable repository, represents abstract repository that can be searched with provided Query
  */
trait SearchableRepository[T] {

  def find(query: Query): Enumerator[T]

}
