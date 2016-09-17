package com.clemble.query.model

import com.clemble.query.core.model.Exclude
import com.clemble.query.core.parser.ExcludeProjectionParser
import org.specs2.mutable.Specification

class ExcludeProjectionSpec extends Specification {

  val parser = new ExcludeProjectionParser()

  "defined " in {
    parser.isDefinedAt("fields-ex" -> Seq("A")) shouldEqual true
    parser.isDefinedAt("fields-ex" -> Seq.empty) shouldEqual true
    parser.isDefinedAt("f" -> Seq.empty) shouldEqual false
  }

  "inlcude" in {
    parser("fields-ex" -> Seq.empty) shouldEqual List.empty
    parser("fields-ex" -> Seq("A")) shouldEqual List(Exclude("A"))
    parser("fields-ex" -> Seq("A,B")) shouldEqual List(Exclude("A"), Exclude("B"))
    parser("fields-ex" -> Seq("A","B")) shouldEqual List(Exclude("A"), Exclude("B"))
  }

  "invalidates" in {
    parser("f" -> Seq.empty) should throwA[IllegalArgumentException]
  }


}
