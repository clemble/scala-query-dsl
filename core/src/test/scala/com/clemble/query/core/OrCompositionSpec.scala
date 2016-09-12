package com.clemble.query.core

import org.specs2.mutable.Specification

class OrCompositionSpec extends Specification {

  "exp or exp" in {
    val fExp = Equals("A", "B")
    val sExp = Equals("B", "C")
    fExp or sExp shouldEqual Or(List(fExp, sExp))
    sExp or fExp shouldEqual Or(List(sExp, fExp))
  }

  "Or's composition with exp" in {
    val fExp = Equals("A", "B")
    val fOrExp = Or(List(fExp))
    val sExp = Equals("B", "C")
    fOrExp or sExp shouldEqual Or(List(sExp, fExp))
    sExp or fOrExp shouldEqual Or(List(sExp, fExp))
  }

  "Or's composition with Or" in {
    val fExp = Equals("A", "B")
    val fOrExp = Or(List(fExp))
    val sExp = Equals("B", "C")
    val sOrExp = Or(List(sExp))
    fOrExp or sOrExp shouldEqual Or(List(fExp, sExp))
    sOrExp or fOrExp shouldEqual Or(List(sExp, fExp))
  }

}
