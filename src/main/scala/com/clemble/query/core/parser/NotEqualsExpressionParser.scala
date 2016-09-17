package com.clemble.query.core.parser

import com.clemble.query.core.model.{NotEquals, Empty, Expression}
import com.clemble.query.core.NotEquals

/**
  * Not Equals Expression parser
  * 1 Single value NOT equals query
  *    As in Equals only String supported
  *
  *    Example:
  *      ?name-ne=A
  *    will be translated as NotEquals("name", "A")
  *
  * 2. Multi value not equals query
  *
  *    Example:
  *      ?name-ne=A&name-ne=B
  *    will be translated as And(NotEquals("name", "A"), NotEquals("name", "B"))
  *
  */
case class NotEqualsExpressionParser(notEqualsParam: String = "-ne") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(notEqualsParam) && !x._2.isEmpty

  override def apply(x: (String, Seq[String])) = {
    val (key, values) = x
    if (!key.endsWith(notEqualsParam))
      throw new IllegalArgumentException(s"Ending must be $notEqualsParam")
    val keyStr = key.substring(0, key.length - notEqualsParam.length)
    values.
      map(NotEquals(keyStr, _)).
      foldRight[Expression](Empty)((a, b) => a.and(b))
  }

}
