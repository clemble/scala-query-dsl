package com.clemble.query.string

import com.clemble.query.core._

trait ExpressionParser {

  val query: PartialFunction[(String, Seq[String]), Expression]

}

/**
  * Not Equals Mongo Query constructor
  * 1 Single value NOT equals query
  *    As in Equals only 2 types of parameters supported String & Boolean
  *
  *    Example:
  *      ?name-ne=GE
  *    will be translated as { "name" : { "$ne" : "GE"}}
  *
  * 2. Multi value not equals query
  *
  *    Example:
  *      ?name-ne=GE&name-ne=DMR
  *    will be translated as { "name" : { "$nin" : ["GE", "DMR"] }}
  *
  */
case class NotEqualsExpressionParser(notEqualsParam: String = "-ne") extends ExpressionParser {

  override val query: PartialFunction[(String, Seq[String]), Expression] = {
    case (key, Seq(x)) if key.endsWith(notEqualsParam) =>
      NotEquals(key.substring(0, key.length - notEqualsParam.length), x)
    case (key, values) if key.endsWith(notEqualsParam) =>
      values.map(NotEquals(key.substring(0, key.length - notEqualsParam.length), _)).
        foldRight[Expression](Empty)((a, b) => a.and(b))
  }

}


/**
  * Default Mongo Query Constructor (simple equals)
  * 1 Single value equals query
  * Currently only 2 types of parameters supported String & Boolean
  *
  *    Example:
  *      ?name=GE&required=false
  *    will be translated as { "name" : "GE", "required" : false }
  *
  * 2. Multi value equals query
  * For this only String parameters supported
  *
  *    Example:
  *      ?name=GE&name=General Electric
  *    will be translated as { "name" : { "$in": ["GE", "General Electric"]}
  */
case object EqualsExpressionParser extends ExpressionParser {

  override val query: PartialFunction[(String, Seq[String]), Expression] = {
    case (key, values) =>
      values.map(Equals(key, _)).foldRight[Expression](Empty)((a, b) => a.and(b))
  }

}

/**
  *  Greater then queries
  *  Similar to less then, all value will be treated as numbers
  *
  *    Example:
  *      ?price-gte=100
  *    will be translated as { "price" : { "gte" : 100 }}
  *
  *    If multiple query parameters were specified, the highest value will be taken
  *
  *    Example:
  *      ?price-gte=100&price-gte=190
  *    will be translated as { "price" : { "gte" : 190 }}
  */
case class GreaterThenEqualsMongoQueryConstructor(gteParam: String = "-gte") extends ExpressionParser {

  override val query: PartialFunction[(String, Seq[String]), Expression] = {
    case (key, values) if key.endsWith(gteParam) =>
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
  *   will be translated as { "price" : { "$lte" : 100 }}
  *
  *   If multiple query parameters were specified, the lowest value will be taken
  *
  *   Example:
  *    ?price-lte=100&price-lte=50
  *   will be translated as { "price" : { "$lte" : 50 }}
  *
  *   If value for the date is stored as long in mongo, $lte would also work with millisecond query
  *
  *   Example:
  *    ?purchaseDate-lte=1231424124244
  *   will be translated as { "purchaseDate" : { "$lte" : 1231424124244 }}
  */
case class LessThenEqualsMongoQueryConstructor(lteParam: String = "-lte") extends ExpressionParser {

  override val query: PartialFunction[(String, Seq[String]), Expression] = {
    case (key, values) if key.endsWith(lteParam) =>
      val minValue = values.map(BigDecimal(_)).min
      LessThenEquals(key.substring(0, key.length - lteParam.length), minValue)
  }

}