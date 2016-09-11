package com.clemble.query.string

import org.specs2.mutable.Specification

class NotEqualsExpressionParserSpec extends Specification {

  val parser = new NotEqualsExpressionParser()

  "single query" in {
    parser.isDefinedAt(("some-ne", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-ne", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

}
