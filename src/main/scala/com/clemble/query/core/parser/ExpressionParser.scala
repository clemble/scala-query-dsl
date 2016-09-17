package com.clemble.query.core.parser

import com.clemble.query.core.model.Expression

/**
  * Created by mavarazy on 9/17/16.
  */
trait ExpressionParser extends PartialFunction[(String, Seq[String]), Expression] {

}
