package com.clemble.query.core.parser

import com.clemble.query.core.model.{And, Empty, NotEquals}
import org.specs2.mutable.Specification

class NotEqualsExpressionParserSpec extends Specification {

  val parser = new NotEqualsExpressionParser()

  "defined works only for postfix" in {
    parser.isDefinedAt(("some-ne", Seq.empty)) shouldEqual false
    parser.isDefinedAt(("some-ne", Seq("A"))) shouldEqual true
    parser.isDefinedAt(("some", Seq("A"))) shouldEqual false
  }

  "reads values" in {
    parser("some-ne" -> Seq()) shouldEqual Empty
    parser("some-ne" -> Seq("A")) shouldEqual NotEquals("some", "A")
    parser("some-ne" -> Seq("A", "B")) shouldEqual And(NotEquals("some", "A"), NotEquals("some", "B"))
  }

  "invalidates" in {
    parser("some" -> Seq("A")) should throwA[IllegalArgumentException]
  }

}
