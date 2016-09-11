package com.clemble.query.string

import com.clemble.query.core._

trait ExpressionParser extends PartialFunction[(String, Seq[String]), Expression] {

}

/**
  * Not Equals Expression parser
  * 1 Single value NOT equals query
  *    As in Equals only String supported
  *
  *    Example:
  *      ?name-ne=GE
  *    will be translated as NotEquals("name", "GE")
  *
  * 2. Multi value not equals query
  *
  *    Example:
  *      ?name-ne=GE&name-ne=DMR
  *    will be translated as And(NotEquals("name", "GE"), NotEquals("name", "DMR"))
  *
  */
case class NotEqualsExpressionParser(notEqualsParam: String = "-ne") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(notEqualsParam) && !x._2.isEmpty

  override def apply(x: (String, Seq[String])) = {
    x match {
      case (key, Seq(x)) =>
        val keyStr = key.substring(0, key.length - notEqualsParam.length)
        NotEquals(keyStr, x)
      case (key, values)=>
        val keyStr = key.substring(0, key.length - notEqualsParam.length)
        values.
          map(NotEquals(keyStr, _)).
          foldRight[Expression](Empty)((a, b) => a.and(b))
    }
  }

}


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

  override def isDefinedAt(x: (String, Seq[String])) = true

  override def apply(x :(String, Seq[String])) = x match {
    case (key, Seq(x)) =>
      Equals(key, x)
    case (key, values) =>
      values.map(Equals(key, _)).foldRight[Expression](Empty)((a, b) => a.and(b))
  }

}

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

  override def apply(x: (String, Seq[String])): Expression = x match {
    case (key, values) =>
      val maxValue = values.map(BigDecimal(_)).max
      GreaterThenEquals(key.substring(0, key.length - gteParam.length), maxValue)
  }

}

/**
  *   Less then queries
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
  *
  *    ?purchaseDate-lte=1231424124244
  *   will be translated as LessThenEquals("purchaseDate", 1231424124244)
  */
case class LessThenEqualsExpressionParser(lteParam: String = "-lte") extends ExpressionParser {

  override def isDefinedAt(x: (String, Seq[String])) = x._1.endsWith(lteParam)

  override def apply(x : (String, Seq[String])): Expression = x match {
    case (key, values) =>
      val minValue = values.map(BigDecimal(_)).min
      LessThenEquals(key.substring(0, key.length - lteParam.length), minValue)
  }

}