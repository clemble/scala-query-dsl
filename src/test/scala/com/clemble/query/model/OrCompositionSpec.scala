package com.clemble.query.model

import org.specs2.mutable.Specification

class OrCompositionSpec extends Specification {

  "exp or exp" in {
    val fExp = Equals("A", "B")
    val sExp = Equals("B", "C")
    fExp or sExp shouldEqual Or(fExp, sExp)
    sExp or fExp shouldEqual Or(sExp, fExp)
  }

  "Or's composition with exp" in {
    val fExp = Equals("A", "B")
    val fOrExp = Or(fExp)
    val sExp = Equals("B", "C")
    fOrExp or sExp shouldEqual Or(sExp, fExp)
    sExp or fOrExp shouldEqual Or(sExp, fExp)
  }

  "Or's composition with Or" in {
    val fExp = Equals("A", "B")
    val fOrExp = Or(fExp)
    val sExp = Equals("B", "C")
    val sOrExp = Or(sExp)
    fOrExp or sOrExp shouldEqual Or(fExp, sExp)
    sOrExp or fOrExp shouldEqual Or(sExp, fExp)
  }

}
