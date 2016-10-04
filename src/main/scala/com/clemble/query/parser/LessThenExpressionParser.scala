package com.clemble.query.parser

import com.clemble.query.model.{Empty, Expression, LessThen}

/**
  *   Less then queries
  *   All values will be treated as numbers
  *
  *   Example:
  *    ?price-lt=100
  *   will be translated as LessThen("price", 100)
  *
  *   If multiple query parameters were specified, the lowest value will be taken
  *
  *   Example:
  *    ?price-lt=100&price-lt=50
  *   will be translated as LessThen("price", 50)
  */
case class LessThenExpressionParser(ltParam: String = "-lt") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(ltParam)

  override def apply(x : (String, Seq[String])): Expression = {
    val (key, values) = x
    if (!key.endsWith(ltParam))
      throw new IllegalArgumentException(s"Ending must be $ltParam")
    if (values.isEmpty)
      return Empty
    val maxValue = values.map(Integer.valueOf(_)).min
    LessThen(key.substring(0, key.length - ltParam.length), maxValue)
  }

}
