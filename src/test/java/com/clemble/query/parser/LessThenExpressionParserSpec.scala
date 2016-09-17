package com.clemble.query.parser

import com.clemble.query.model.{LessThen, Empty}
import org.specs2.mutable.Specification

class LessThenExpressionParserSpec extends Specification {

  val parser = LessThenExpressionParser()

  "defined works only for postfix" in {
    parser.isDefinedAt(("some-lte", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-lt", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

  "reads values" in {
    parser("some-lt" -> Seq()) shouldEqual Empty
    parser("some-lt" -> Seq("100")) shouldEqual LessThen("some", BigDecimal(100))
    parser("some-lt" -> Seq("100", "190")) shouldEqual LessThen("some", BigDecimal(100))
  }

  "invalidates" in {
    parser("some" -> Seq("A")) should throwA[IllegalArgumentException]
    parser("some-lt" -> Seq("A")) should throwA[IllegalArgumentException]
  }

}
