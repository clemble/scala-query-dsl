package com.clemble.query.string

import com.clemble.query.core.{Descending, Ascending}
import org.specs2.mutable.Specification

class SortOrderParserSpec extends Specification {

  val parser = new SortOrderParser()

  "defined only at sort" in {
    parser.isDefinedAt("sort" -> Seq.empty[String]) shouldEqual true
    parser.isDefinedAt("sort!" -> Seq.empty[String]) shouldEqual false
    parser.isDefinedAt("!sort" -> Seq.empty[String]) shouldEqual false
  }

  "ASC by default" in {
    parser.apply("sort" -> Seq("A")) shouldEqual List(Ascending("A"))
  }

  "DSC support" in {
    parser.apply("sort" -> Seq("!A")) shouldEqual List(Descending("A"))
  }

  "read combinations" in {
    parser.apply("sort" -> Seq("A", "!B")) shouldEqual List(Ascending("A"), Descending("B"))
  }

}
