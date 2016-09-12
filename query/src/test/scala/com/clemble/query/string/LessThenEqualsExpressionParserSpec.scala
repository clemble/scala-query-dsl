package com.clemble.query.string

import com.clemble.query.core.{LessThenEquals, Empty, GreaterThenEquals}
import org.specs2.mutable.Specification

class LessThenEqualsExpressionParserSpec extends Specification {

  val parser = LessThenEqualsExpressionParser()

  "defined works only for postfix" in {
    parser.isDefinedAt(("some-lt", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-lte", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

  "reads values" in {
    parser("some-lte" -> Seq()) shouldEqual Empty
    parser("some-lte" -> Seq("100")) shouldEqual LessThenEquals("some", BigDecimal(100))
    parser("some-lte" -> Seq("100", "190")) shouldEqual LessThenEquals("some", BigDecimal(100))
  }

  "invalidates" in {
    parser("some" -> Seq("A")) should throwA[IllegalArgumentException]
    parser("some-lte" -> Seq("A")) should throwA[IllegalArgumentException]
  }

}
