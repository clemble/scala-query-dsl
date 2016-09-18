package com.clemble.query.model

import org.specs2.mutable.Specification

class PaginationParamsSpec extends Specification {

  "invalidate wrong pagination" in {
    new PaginationParams(-1, 0) should throwA[IllegalArgumentException]
    new PaginationParams(0, 0) should throwA[IllegalArgumentException]
  }

  "calculate offset" in {
    new PaginationParams(0, 10).offset() shouldEqual 0
    new PaginationParams(1, 10).offset() shouldEqual 10
    new PaginationParams(1, 100).offset() shouldEqual 100
  }

  "calculate limit" in {
    new PaginationParams(0, 10).limit() shouldEqual 10
    new PaginationParams(1, 10).limit() shouldEqual 20
    new PaginationParams(1, 100).limit() shouldEqual 200
    new PaginationParams(0, Int.MaxValue).limit() shouldEqual Int.MaxValue
  }
}
