package com.clemble.query.string

import com.clemble.query.core.PaginationParams
import org.specs2.mutable.Specification

class PaginationParamsParserSpec extends Specification {

  val parser = new PaginationParamsParser()

  "reading page size" in {
    val query = Map(
      "page_from" -> Seq("1")
    )
    parser.toPage(query) shouldEqual PaginationParams(1, 25)
  }

  "reading page" in {
    val query = Map(
      "page_size" -> Seq("100")
    )
    parser.toPage(query) shouldEqual PaginationParams(0, 100)
  }

  "reading default page" in {
    val query = Map.empty[String, Seq[String]]
    parser.toPage(query) shouldEqual PaginationParams(0, Int.MaxValue)
  }

}
