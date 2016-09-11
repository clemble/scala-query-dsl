package com.clemble.query.core

/**
  * Query expression
  */
sealed trait Expression {

  def and(expression: Expression) = {
    this match {
      case Empty => expression
      case And(expressions) => And(expression :: expressions)
      case exp => And(List(expression, exp))
    }
  }

  def or(expression: Expression) = {
    this match {
      case Empty => expression
      case Or(expressions) => Or(expression :: expressions)
      case exp => Or(List(expression, exp))
    }
  }

}

case object Empty extends Expression

sealed trait BooleanExpression extends Expression

case class And(conditions: List[Expression]) extends BooleanExpression
case class Or(conditions: List[Expression]) extends BooleanExpression

case class Equals(field: String, value: String) extends Expression
case class NotEquals(field: String, value: String) extends Expression

sealed trait ArithmeticExpression

case class GreaterThen(field: String, value: Number) extends Expression
case class GreaterThenEquals(field: String, value: Number) extends Expression
case class LessThen(field: String, value: Number) extends Expression
case class LessThenEquals(field: String, value: Number) extends Expression


