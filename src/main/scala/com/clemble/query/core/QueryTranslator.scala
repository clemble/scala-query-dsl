package com.clemble.query.core

import com.clemble.query.core.model.Expression
import play.api.libs.json.JsObject

trait QueryTranslator {

  def translate(query: Expression): JsObject

}
