package com.clemble.query.core.parser

import com.clemble.query.core.model.{Exclude, Projection}

/**
  * Basic extraction of projection configurations
  *
  * It supports projections parameters provided through REST query string
  * There are 2 parameters that are used for field inclusion & exclusion "fields" & "fields-ex" appropriately:
  *   Example:
  *      ?fields=name&fields=model
  *   will be interpreted as { "name" : 1, "model" : 1}, and query would return a json object with only name & model
  *
  *   Example:
  *      ?fields-ex=name&fields-ex=model
  *   will be interpreted as { "name": 0, "model" : 0} and query would return a json with all fields set except for name & model
  *
  *  Extension allows to specify query parameters in different formats
  *
  *   ?fields=name,model
  *   Is the same as
  *   ?fields=name&fields=model
  *
  *   or
  *
  *   ?fields-ex=name,model
  *   Is the same as
  *   ?fields-ex=name&fields-ex=model&sort=!year
  *
  * @return valid projection
  */
case class ExcludeProjectionParser(excludeParam: String = "fields-ex") extends PartialFunction[(String, Seq[String]), List[Projection]] {

  override def isDefinedAt(x: (String, Seq[String])): Boolean = x._1 == excludeParam

  def apply(query: (String, Seq[String])): List[Projection] = {
    val (key, fields) = query
    if (key != excludeParam)
      throw new IllegalArgumentException(s"Expected $excludeParam instead of $key")
    val allExcludedProjections = fields.
      flatMap(_.split(",")).
      map(_.trim()).
      map(field => Exclude(field))
    allExcludedProjections.toList
  }

}
