package com.clemble.query

import com.clemble.query.model._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, JsNumber, Format, JsObject}
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import reactivemongo.play.iteratees.cursorProducer

import scala.concurrent.{Future, ExecutionContext}

trait MongoBSONSearchableRepository[T] extends SearchableRepository[T]{

  val queryTranslator: QueryTranslator[BSONDocument]
  val collection: BSONCollection
  implicit val f: BSONDocumentReader[T]

  override def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.one[T]
  }

  override def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T] = {
    val mongoQuery = buildQuery(query)
    mongoQuery.cursor[T]().enumerator(maxDocs = query.pagination.pageSize)
  }

  private def buildQuery(query: Query)(implicit ec: ExecutionContext): GenericQueryBuilder[collection.pack.type]#Self = {
    val mongoQuery = queryTranslator.translate(query.where)
    val sortQuery = queryTranslator.translateSort(query.sort)
    collection.find(mongoQuery).sort(sortQuery).options(QueryOpts(skipN = query.pagination.offset()))
  }

}

/**
  * Default mongo BSON QueryTranslator
  */
class MongoBSONQueryTranslator extends QueryTranslator[BSONDocument] {

  override def translate(query: Expression): BSONDocument = {
    query match {
      case And(conditions) =>
        BSONDocument(conditions.map(translate).flatMap(obj => obj.elements))
      case Or(conditions) =>
        BSONDocument("$or" -> conditions.map(translate))
      case NotEquals(field, value) =>
        BSONDocument(field -> BSONDocument("$ne" -> value))
      case Equals(field, value) =>
        BSONDocument(field -> value)
      case LessThen(field, value) =>
        BSONDocument(field -> BSONDocument("$lt" -> BSONInteger(value.toInt)))
      case LessThenEquals(field, value) =>
        BSONDocument(field -> BSONDocument("$lte" -> BSONInteger(value.toInt)))
      case GreaterThen(field, value) =>
        BSONDocument(field -> BSONDocument("$gt" -> BSONInteger(value.toInt)))
      case GreaterThenEquals(field, value) =>
        BSONDocument(field -> BSONDocument("$gte" -> BSONInteger(value.toInt)))
      case Empty =>
        BSONDocument()
    }
  }

  override def translateSort(sorts: List[SortOrder]): BSONDocument = {
    val sortFields = sorts.map({
      case Ascending(field) => field -> BSONInteger(1)
      case Descending(field) => field -> BSONInteger(-1)
    })
    BSONDocument(sortFields)
  }

}
