package com.clemble.query.core.parser

import com.clemble.query.core.model.{Ascending, Descending}
import org.specs2.mutable.Specification

class SortOrderParserSpec extends Specification {

  val parser = new SortOrderParser()

  "defined only at sort" in {
    parser.isDefinedAt("sort" -> Seq.empty[String]) shouldEqual true
    parser.isDefinedAt("sort!" -> Seq.empty[String]) shouldEqual false
    parser.isDefinedAt("!sort" -> Seq.empty[String]) shouldEqual false

  }

  "not accept other params" in {
    parser("yo" -> Seq.empty) should throwA[IllegalArgumentException]
  }

  "ignore empty" in {
    parser("sort" -> Seq.empty) shouldEqual List.empty
  }

  "ASC by default" in {
    parser("sort" -> Seq("A")) shouldEqual List(Ascending("A"))
  }

  "DSC support" in {
    parser("sort" -> Seq("!A")) shouldEqual List(Descending("A"))
  }

  "read combinations" in {
    parser("sort" -> Seq("A", "!B")) shouldEqual List(Ascending("A"), Descending("B"))
  }

}
