package com.clemble.query

import com.clemble.query.model._
import com.sksamuel.elastic4s._
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.iteratee.Enumerator
import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}
import com.sksamuel.elastic4s.ElasticDsl._

trait ElasticSearchableRepository[T] extends SearchableRepository[T] {

  private val ELASTICSEARCH_QUERY_LIMIT = 10000

  val queryTranslator: QueryTranslator[QueryDefinition, List[SortDefinition]]
  val client: ElasticClient
  val indexAndType: IndexAndType
  implicit val format: HitAs[T]

  override def findOne(query: Query)(implicit ec: ExecutionContext): Future[Option[T]] = {
    val searchQuery = buildQuery(query)
    val fFirstResult = client.execute(searchQuery).map(readHits).map(_.headOption)
    fFirstResult
  }

  override def find(query: Query)(implicit ec: ExecutionContext): Enumerator[T] = {
    val searchQuery = buildQuery(query)
    val fSearchResults = client.execute(searchQuery).map(readHits)
    Enumerator.flatten(fSearchResults.map(res => Enumerator.enumerate(res)))
  }


  private def buildQuery(query: Query): SearchDefinition = {
    val filters = queryTranslator.translate(query.where)
    val esQuery = search(indexAndType).query(
      filter(filters)
    )
    val sorts = queryTranslator.translateSort(query.sort)
    val limit = query.pagination.limitWithMax(ELASTICSEARCH_QUERY_LIMIT)
    esQuery.sort(sorts :_*).from(query.pagination.offset()).limit(limit)
  }

  private def readHits(searchResponse: RichSearchResponse): mutable.ArraySeq[T] = {
    searchResponse.hits.map(hit => format.as(hit))
  }

}

class ElasticSearchQueryTranslator extends QueryTranslator[QueryDefinition, List[SortDefinition]] {

  override def translate(where: Expression): QueryDefinition = {
    where match {
      case And(conditions) =>
        must(conditions.map(translate))
      case Or(conditions) =>
        should(conditions.map(translate)).minimumShouldMatch(1)
      case NotEquals(field, value) =>
        not(termQuery(field, value))
      case Equals(field, value) =>
        termQuery(field, value)
      case LessThen(field, value) =>
        rangeQuery(field).lte(value.toString()).includeUpper(false)
      case LessThenEquals(field, value) =>
        rangeQuery(field).lte(value.toString()).includeUpper(true)
      case GreaterThen(field, value) =>
        rangeQuery(field).gte(value.toString()).includeLower(false)
      case GreaterThenEquals(field, value) =>
        rangeQuery(field).gte(value.toString()).includeLower(true)
      case Empty =>
        query.all
    }
  }

  override def translateSort(sorts: List[model.SortOrder]): List[SortDefinition] = {
    sorts.map(_ match {
      case Ascending(f) =>
        field sort f order SortOrder.ASC
      case Descending(f) =>
        field sort f order SortOrder.DESC
    })
  }
}
