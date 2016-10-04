package com.clemble.query.parser

import com.clemble.query.model.{LessThenEquals, Empty, Expression}

/**
  *   Less then equals queries
  *   All values will be treated as numbers
  *
  *   Example:
  *    ?price-lte=100
  *   will be translated as LessThenEquals("price", 100)
  *
  *   If multiple query parameters were specified, the lowest value will be taken
  *
  *   Example:
  *    ?price-lte=100&price-lte=50
  *   will be translated as LessThenEquals("price", 50)
  */
case class LessThenEqualsExpressionParser(lteParam: String = "-lte") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(lteParam)

  override def apply(x : (String, Seq[String])): Expression = {
    val (key, values) = x
    if (!key.endsWith(lteParam))
      throw new IllegalArgumentException(s"Ending must be $lteParam")
    if (values.isEmpty)
      return Empty
    val maxValue = values.map(Integer.valueOf(_)).min
    LessThenEquals(key.substring(0, key.length - lteParam.length), maxValue)
  }

}
