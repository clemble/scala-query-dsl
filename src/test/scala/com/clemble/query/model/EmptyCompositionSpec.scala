package com.clemble.query.model

import org.specs2.mutable.Specification

class EmptyCompositionSpec extends Specification {

  "Empty and Expression is original Expression" in {
    val exp = NotEquals("A", "B")
    exp and Empty shouldEqual exp
    Empty and exp shouldEqual exp
  }

  "Empty or Expression is original Expression" in {
    val exp = NotEquals("A", "B")
    exp or Empty shouldEqual exp
    Empty or exp shouldEqual exp
  }

  "Empty and Empty is Empty" in {
    Empty and Empty shouldEqual Empty
  }

  "Empty or Empty is Empty" in {
    Empty or Empty shouldEqual Empty
  }

}
