package com.clemble.query.model

import scala.math.ScalaNumber

/**
  * Query expression
  */
sealed trait Expression {

  def and(other: Expression) = {
    (this, other) match {
      case (exp, Empty) => exp
      case (Empty, exp) => exp
      case (And(fExps), And(sExps)) => And(fExps ++ sExps)
      case (And(expressions), exp) => And(exp :: expressions)
      case (exp, And(expressions)) => And(exp :: expressions)
      case (fExp, sExp) => And(List(fExp, sExp))
    }
  }

  def or(other: Expression) = {
    (this, other) match {
      case (exp, Empty) => exp
      case (Empty, exp) => exp
      case (Or(fExps), Or(sExps)) => Or(fExps ++ sExps)
      case (Or(expressions), exp) => Or(exp :: expressions)
      case (exp, Or(expressions)) => Or(exp :: expressions)
      case (fExp, sExp) => Or(List(fExp, sExp))
    }
  }

}

case object Empty extends Expression

sealed trait BooleanExpression extends Expression

case class And(conditions: List[Expression]) extends BooleanExpression
object And {
  def apply(conditions: Expression*): And = {
    new And(conditions.toList)
  }
}

case class Or(conditions: List[Expression]) extends BooleanExpression
object Or {
  def apply(conditions: Expression*): Or = {
    new Or(conditions.toList)
  }
}

case class Equals(field: String, value: String) extends Expression
case class NotEquals(field: String, value: String) extends Expression

sealed trait ArithmeticExpression extends Expression

case class IntEquals(field: String, value: Int) extends ArithmeticExpression
case class IntNotEquals(field: String, value: Int) extends ArithmeticExpression

case class GreaterThen(field: String, value: Int) extends ArithmeticExpression
case class GreaterThenEquals(field: String, value: Int) extends ArithmeticExpression
case class LessThen(field: String, value: Int) extends ArithmeticExpression
case class LessThenEquals(field: String, value: Int) extends ArithmeticExpression

object Expression {

  object Implicits {

    implicit class FieldNameQueryBuilder(field: String) {

      def is(value: String) = Equals(field, value)
      def not(value: String) = NotEquals(field, value)

      def is(value: Int) = IntEquals(field, value)
      def not(value: Int) = IntNotEquals(field, value)

      def gt(value: Int) = GreaterThen(field, value)
      def >(value: Int) = GreaterThen(field, value)

      def gte(value: Int) = GreaterThenEquals(field, value)
      def >=(value: Int) = GreaterThenEquals(field, value)

      def lt(value: Int) = LessThen(field, value)
      def <(value: Int) = LessThen(field, value)

      def lte(value: Int) = LessThenEquals(field, value)
      def <=(value: Int) = LessThenEquals(field, value)
    }

  }

}
