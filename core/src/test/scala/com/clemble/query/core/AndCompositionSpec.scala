package com.clemble.query.core

import org.specs2.mutable.Specification

class AndCompositionSpec extends Specification{

  "exp and exp" in {
    val fExp = Equals("A", "B")
    val sExp = Equals("B", "C")
    fExp and sExp shouldEqual And(List(fExp, sExp))
    sExp and fExp shouldEqual And(List(sExp, fExp))
  }

  "And's composition with exp" in {
    val fExp = Equals("A", "B")
    val fAndExp = And(List(fExp))
    val sExp = Equals("B", "C")
    fAndExp and sExp shouldEqual And(List(sExp, fExp))
    sExp and fAndExp shouldEqual And(List(sExp, fExp))
  }

  "And's composition with and" in {
    val fExp = Equals("A", "B")
    val fAndExp = And(List(fExp))
    val sExp = Equals("B", "C")
    val sAndExp = And(List(sExp))
    fAndExp and sAndExp shouldEqual And(List(fExp, sExp))
    sAndExp and fAndExp shouldEqual And(List(sExp, fExp))
  }

}
