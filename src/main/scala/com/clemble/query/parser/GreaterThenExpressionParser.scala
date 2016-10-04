package com.clemble.query.parser

import com.clemble.query.model.{Empty, Expression, GreaterThen}

/**
  *  Greater then queries
  *  Similar to less then, all value will be treated as numbers
  *
  *    Example:
  *      ?price-gt=100
  *    will be translated as GreaterThen("price", 100)
  *
  *    If multiple query parameters were specified, the highest value will be taken
  *
  *    Example:
  *      ?price-gt=100&price-gt=190
  *    will be translated as GreaterThen("price", 190)
  */
case class GreaterThenExpressionParser(gtParam: String = "-gt") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(gtParam)

  override def apply(x: (String, Seq[String])): Expression = {
    val (key, values) = x
    if (!key.endsWith(gtParam))
      throw new IllegalArgumentException(s"Ending must be $gtParam")
    if (values.isEmpty)
      return Empty
    val maxValue = values.map(Integer.valueOf(_)).max
    GreaterThen(key.substring(0, key.length - gtParam.length), maxValue)
  }

}
