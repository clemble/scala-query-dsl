package com.clemble.query.model

trait ExpressionField {
  val field: String

  def asc = Ascending(field)
  def desc = Descending(field)
}

case class StringField(field: String) extends ExpressionField {

  def is(str: String): Expression = Equals(field, str)
  def not(str: String): Expression = NotEquals(field, str)

}

case class IntField(field: String) extends ExpressionField {

  def is(num: Int) = IntEquals(field, num)
  def not(num: Int) = IntNotEquals(field, num)

  def lt(num: Int) = LessThen(field, num)
  def <(num: Int) = lt(num)

  def lte(num: Int) = LessThenEquals(field, num)
  def <=(num: Int) = lte(num)

  def gt(num: Int) = GreaterThen(field, num)
  def > (num: Int) = gt(num)

  def gte(num: Int) = GreaterThenEquals(field, num)
  def >= (num: Int) = gte(num)

}
