package com.clemble.query.core.parser

import com.clemble.query.core.model.{Empty, GreaterThen}
import org.specs2.mutable.Specification

class GreaterThenExpressionParserSpec extends Specification{

  val parser = new GreaterThenExpressionParser()

  "defined works only for postfix" in {
    parser.isDefinedAt(("some-gte", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-gt", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

  "reads values" in {
    parser("some-gt" -> Seq()) shouldEqual Empty
    parser("some-gt" -> Seq("100")) shouldEqual GreaterThen("some", BigDecimal(100))
    parser("some-gt" -> Seq("100", "190")) shouldEqual GreaterThen("some", BigDecimal(190))
  }

  "invalidates" in {
    parser("some" -> Seq("A")) should throwA[IllegalArgumentException]
    parser("some-gt" -> Seq("A")) should throwA[IllegalArgumentException]
  }

}
