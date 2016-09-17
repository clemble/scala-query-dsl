package com.clemble.query.core.parser

import com.clemble.query.core.model._
import org.specs2.mutable.Specification

class QueryParserSpec extends Specification {

  val parser = QueryParser.DEFAULT

  "parse sort ASC" in {
    val query = Map(
      "sort" -> Seq("A")
    )
    parser.parse(query) shouldEqual Query(where = Empty, sort = List(Ascending("A")))
  }

  "parse sort DESC" in {
    val query = Map(
      "sort" -> Seq("!A")
    )
    parser.parse(query) shouldEqual Query(where = Empty, sort = List(Descending("A")))
  }

  "parse exclude" in {
    val query = Map(
      "fields-ex" -> Seq("A")
    )
    parser.parse(query) shouldEqual Query(where = Empty, projection = List(Exclude("A")))
  }

  "parse include" in {
    val query = Map(
      "fields" -> Seq("A")
    )
    parser.parse(query) shouldEqual Query(where = Empty, projection = List(Include("A")))
  }

  "parse pagination" in {
    val query = Map(
      "page_from" -> Seq("0")
    )
    parser.parse(query) shouldEqual Query(where = Empty, pagination = PaginationParams(0, 25))
  }

  "parse pagination size" in {
    val query = Map(
      "page_size" -> Seq("10")
    )
    parser.parse(query) shouldEqual Query(where = Empty, pagination = PaginationParams(0, 10))
  }

  "parse query" in {
    val query = Map(
      "a-ne" -> Seq("A"),
      "b-lt" -> Seq("10"),
      "c-lte" -> Seq("100"),
      "d-gt" -> Seq("90"),
      "e-gte" -> Seq("100"),
      "f" -> Seq("B")
    )
    parser.parse(query) shouldEqual Query(where =
      And(
        GreaterThenEquals("e", BigDecimal(100)),
        GreaterThen("d", BigDecimal(90)),
        LessThenEquals("c", BigDecimal(100)),
        NotEquals("a", "A"),
        LessThen("b", BigDecimal(10)),
        Equals("f", "B")
      )
    )
  }


}
