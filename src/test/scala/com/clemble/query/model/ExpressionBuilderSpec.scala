package com.clemble.query.model

import org.specs2.mutable.Specification
import com.clemble.query.model.Expression.Implicits._

class ExpressionBuilderSpec extends Specification{

  "equals" in {
    "a" is "A" shouldEqual Equals("a", "A")
  }

  "not equals" in {
    "a" not "B" shouldEqual NotEquals("a", "B")
  }

  "less then" in {
    "a" < 1 shouldEqual LessThen("a", 1)
    "a" lt 1 shouldEqual LessThen("a", 1)
  }

  "less then equal" in {
    "a" <= 1 shouldEqual LessThenEquals("a", 1)
    "a" lte 1 shouldEqual LessThenEquals("a", 1)
  }

  "greater then" in {
    "a" > 1 shouldEqual GreaterThen("a", 1)
    "a" gt 1 shouldEqual GreaterThen("a", 1)
  }

  "greater then equal" in {
    "a" >= 1 shouldEqual GreaterThenEquals("a", 1)
    "a" gte 1 shouldEqual GreaterThenEquals("a", 1)
  }

  "composition" in {
    ("a" is "A") and ("b" not "B") and ("c" is "C") shouldEqual And(Equals("c", "C"), Equals("a", "A"), NotEquals("b", "B"))
    ("a" is "A") or ("b" not "B") shouldEqual Or(Equals("a", "A"), NotEquals("b", "B"))
  }

}
