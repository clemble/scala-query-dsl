package com.clemble.query.parser

import com.clemble.query.model.{Empty, Expression, GreaterThenEquals}

/**
  *  Greater then equals queries
  *  Similar to less then, all value will be treated as numbers
  *
  *    Example:
  *      ?price-gte=100
  *    will be translated as GreaterThenEquals("price", 100)
  *
  *    If multiple query parameters were specified, the highest value will be taken
  *
  *    Example:
  *      ?price-gte=100&price-gte=190
  *    will be translated as GreaterThenEquals("price", 190)
  */
case class GreaterThenEqualsExpressionParser(gteParam: String = "-gte") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(gteParam)

  override def apply(x: (String, Seq[String])): Expression = {
    val (key, values) = x
    if (!key.endsWith(gteParam))
      throw new IllegalArgumentException(s"Ending must be $gteParam")
    if (values.isEmpty)
      return Empty
    val maxValue = values.map(BigDecimal(_)).max
    GreaterThenEquals(key.substring(0, key.length - gteParam.length), maxValue)
  }

}
