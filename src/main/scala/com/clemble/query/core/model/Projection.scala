package com.clemble.query.core.model

/**
  * Simple abstraction to represent field Projection
  */
sealed trait Projection
case class Include(field: String) extends Projection
case class Exclude(field: String) extends Projection