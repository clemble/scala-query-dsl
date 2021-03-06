package com.clemble.query.parser

import com.clemble.query.model.{Equals, Empty}
import org.specs2.mutable.Specification

class EqualsExpressionParserSpec extends Specification {

  val parser = EqualsExpressionParser

  "defined works only for postfix" in {
    parser.isDefinedAt(("some", Seq.empty)) shouldEqual true
    parser.isDefinedAt(("some-ne", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some-gte", Seq("A"))) shouldEqual true
  }

  "reads values" in {
    parser("some" -> Seq()) shouldEqual Empty
    parser("some" -> Seq("A")) shouldEqual Equals("some", "A")
  }

  "invalidates" in {
    parser("some" -> Seq("A", "B")) should throwA[IllegalArgumentException]
  }

}
