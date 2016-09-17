package com.clemble.query.model

import org.specs2.mutable.Specification

class AndCompositionSpec extends Specification {

  "exp and exp" in {
    val fExp = Equals("A", "B")
    val sExp = Equals("B", "C")
    fExp and sExp shouldEqual And(fExp, sExp)
    sExp and fExp shouldEqual And(sExp, fExp)
  }

  "And's composition with exp" in {
    val fExp = Equals("A", "B")
    val fAndExp = And(fExp)
    val sExp = Equals("B", "C")
    fAndExp and sExp shouldEqual And(sExp, fExp)
    sExp and fAndExp shouldEqual And(sExp, fExp)
  }

  "And's composition with and" in {
    val fExp = Equals("A", "B")
    val fAndExp = And(fExp)
    val sExp = Equals("B", "C")
    val sAndExp = And(sExp)
    fAndExp and sAndExp shouldEqual And(fExp, sExp)
    sAndExp and fAndExp shouldEqual And(sExp, fExp)
  }

}
