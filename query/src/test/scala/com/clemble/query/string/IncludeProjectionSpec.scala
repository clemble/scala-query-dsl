package com.clemble.query.string

import com.clemble.query.core.Include
import org.specs2.mutable.Specification

class IncludeProjectionSpec extends Specification {

  val parser = new IncludeProjectionParser()

  "defined " in {
    parser.isDefinedAt("fields" -> Seq.empty) shouldEqual true
    parser.isDefinedAt("fields" -> Seq("A")) shouldEqual true
    parser.isDefinedAt("f" -> Seq.empty) shouldEqual false
  }

  "inlcude" in {
    parser("fields" -> Seq.empty) shouldEqual List.empty
    parser("fields" -> Seq("A")) shouldEqual List(Include("A"))
    parser("fields" -> Seq("A,B")) shouldEqual List(Include("A"), Include("B"))
    parser("fields" -> Seq("A","B")) shouldEqual List(Include("A"), Include("B"))
  }

  "invalidates" in {
    parser("f" -> Seq.empty) should throwA[IllegalArgumentException]
  }


}
