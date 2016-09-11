package com.clemble.query.string

import com.clemble.query.core.{Exclude, Include, Projection}

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
case class ProjectionParser(includeParam: String = "fields", excludeParam: String = "fields-ex") {

  def toProjection(query: Map[String, Seq[String]]): List[Projection] = {
    val includeProjections = readIncludeParams(query)
    val excludeProjections = readExcludeParams(query)
    includeProjections ++ excludeProjections
  }

  private def readExcludeParams(query: Map[String, Seq[String]]): List[Exclude] = {
    query.get(excludeParam) match {
      case Some(fields) =>
        val allExcludedProjections = fields.
          flatMap(_.split(",")).
          map(_.trim()).
          map(field => Exclude(field))
        allExcludedProjections.toList
      case None =>
        List.empty[Exclude]
    }
  }

  private def readIncludeParams(query: Map[String, Seq[String]]): List[Include] = {
    query.get(includeParam) match {
      case Some(fields) =>
        val allIncludedProjections = fields.
          flatMap(_.split(",")).
          map(_.trim()).
          map(field => Include(field))
        allIncludedProjections.toList
      case None =>
        List.empty[Include]
    }
  }

}
