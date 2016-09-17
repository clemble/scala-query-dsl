package com.clemble.query.parser

import com.clemble.query.model.Expression

/**
  * Created by mavarazy on 9/17/16.
  */
trait ExpressionParser extends PartialFunction[(String, Seq[String]), Expression] {

}
