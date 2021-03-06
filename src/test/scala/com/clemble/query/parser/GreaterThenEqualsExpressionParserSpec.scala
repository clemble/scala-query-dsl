package com.clemble.query.parser

import com.clemble.query.model.{GreaterThenEquals, Empty}
import org.specs2.mutable.Specification

class GreaterThenEqualsExpressionParserSpec extends Specification{

  val parser = new GreaterThenEqualsExpressionParser()

  "defined works only for postfix" in {
    parser.isDefinedAt(("some-gt", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-gte", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

  "reads values" in {
    parser("some-gte" -> Seq()) shouldEqual Empty
    parser("some-gte" -> Seq("100")) shouldEqual GreaterThenEquals("some", 100)
    parser("some-gte" -> Seq("100", "190")) shouldEqual GreaterThenEquals("some", 190)
  }

  "invalidates" in {
    parser("some" -> Seq("A")) should throwA[IllegalArgumentException]
    parser("some-gte" -> Seq("A")) should throwA[IllegalArgumentException]
  }

}
