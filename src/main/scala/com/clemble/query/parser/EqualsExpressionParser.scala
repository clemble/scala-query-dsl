package com.clemble.query.parser

import com.clemble.query.model.{Empty, Equals, Expression}

/**
  * Equals expression parser. Single value equals query
  * Currently only String supported
  *
  *    Example:
  *      ?name=GE
  *    will be translated as Equals("name", "GE")
  *
  * 2. Multi value equals query
  * For this only String parameters supported
  *
  *    Example:
  *      ?name=GE&name=General Electric
  *    will be translated as an error, since this case can't happen
  */
case object EqualsExpressionParser extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._2.length <= 1

  override def apply(x :(String, Seq[String])) = {
    val (key, values) = x
    if (values.length > 1)
      throw new IllegalArgumentException("Can't equal 2 things at the same time")
    values.map(Equals(key, _)).foldRight[Expression](Empty)((a, b) => a.and(b))
  }

}
