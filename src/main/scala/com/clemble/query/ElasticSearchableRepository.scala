package com.clemble.query

import com.clemble.query.model._
import com.sksamuel.elastic4s._
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, JsObject}
import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}
import com.sksamuel.elastic4s.ElasticDsl._

/**
  * ElasticSearch implementation for SearchableRepository
  *
  * In order to use exact match by term, analyer for the field should be set to no_analyzer, check
  * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-term-query.html
  *
  * @tparam T
  */
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

  protected def buildQuery(query: Query): SearchDefinition = {
    val filters = queryTranslator.translate(query.where)
    val esQuery = search(indexAndType).query(
      filter(filters)
    )
    val sorts = queryTranslator.translateSort(query.sort)
    val offset = query.pagination.offset()
    val limit = query.pagination.limitWithMax(ELASTICSEARCH_QUERY_LIMIT) - offset
    esQuery.sort(sorts :_*).from(query.pagination.offset()).limit(limit)
  }

  private def readHits(searchResponse: RichSearchResponse): mutable.ArraySeq[T] = {
    searchResponse.hits.map(hit => format.as(hit))
  }


}

trait ElasticSearchableRepositoryWithProjectionSupport[T] extends ElasticSearchableRepository[T] with ProjectionSupport {

  override def findWithProjection(query: Query)(implicit ec: ExecutionContext): Enumerator[JsObject] = {
    val searchQuery = specifyProjection(buildQuery(query), query)
    val fSearchResults = client.execute(searchQuery).map(readHitsProjection)
    Enumerator.flatten(fSearchResults.map(res => Enumerator.enumerate(res)))
  }

  override def findOneWithProjection(query: Query)(implicit ex: ExecutionContext): Future[Option[JsObject]] = {
    val searchQuery = specifyProjection(buildQuery(query), query)
    val fFirstResult = client.execute(searchQuery).map(readHitsProjection).map(_.headOption)
    fFirstResult
  }

  private def specifyProjection(search: SearchDefinition, query: Query): SearchDefinition = {
    val includeFields = query.projection.collect({ case Include(field) => field })
    val excludeFields = query.projection.collect({ case Exclude(field) => field })
    search.sourceExclude(excludeFields: _*).sourceInclude(includeFields: _*)
  }

  private def readHitsProjection(searchResponse: RichSearchResponse): mutable.ArraySeq[JsObject] = {
    searchResponse.hits.map(hit => {
      Json.parse(hit.sourceAsString).as[JsObject]
    })
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
