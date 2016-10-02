package com.clemble.query

import com.clemble.query.model._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, JsObject}

import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}

class ElasticQueryFactory[T] (
    client: ElasticClient,
    indexAndType: IndexAndType,
    queryTranslator: QueryTranslator[QueryDefinition, List[SortDefinition]] = new ElasticSearchQueryTranslator
)(implicit format: HitAs[T]) extends QueryFactory[T] {

  override def create(exp: Expression): QueryBuilder[T] = {
    val filters = queryTranslator.translate(exp)
    val esQuery = search(indexAndType).query(
      filter(filters)
    )
    new ElasticQueryBuilder[T](esQuery, client)
  }

}

class ElasticQueryBuilder[T](search: SearchDefinition, client: ElasticClient)(implicit format: HitAs[T])  extends QueryBuilder[T] {

  private val ELASTICSEARCH_QUERY_LIMIT = 10000

  override def pagination(pagination: PaginationParams): QueryBuilder[T] = {
    val offset = pagination.offset()
    val limit = pagination.limitWithMax(ELASTICSEARCH_QUERY_LIMIT) - offset
    search.from(offset).limit(limit)
    this
  }

  override def projection(projection: List[Projection]): QueryBuilder[T] = {
    val includeFields = projection.collect({ case Include(field) => field })
    val excludeFields = projection.collect({ case Exclude(field) => field })
    search.sourceExclude(excludeFields: _*).sourceInclude(includeFields: _*)
    this
  }

  override def sorted(sorts: List[model.SortOrder]): QueryBuilder[T] = {
    val sortOrder = sorts.map(_ match {
      case Ascending(f) => field sort f order SortOrder.ASC
      case Descending(f) => field sort f order SortOrder.DESC
    })
    search.sort(sortOrder: _*)
    this
  }

  override def findOne()(implicit ec: ExecutionContext): Future[Option[T]] = {
    val fFirstResult = client.execute(search).map(readHits).map(_.headOption)
    fFirstResult
  }

  override def find()(implicit ec: ExecutionContext): Enumerator[T] = {
    val fSearchResults = client.execute(search).map(readHits)
    Enumerator.flatten(fSearchResults.map(res => Enumerator.enumerate(res)))
  }

  private def readHits(searchResponse: RichSearchResponse): mutable.ArraySeq[T] = {
    searchResponse.hits.map(hit => format.as(hit))
  }

  override def findOneWithProjection()(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    val fFirstResult = client.execute(search).map(readHitsProjection).map(_.headOption)
    fFirstResult
  }

  override def findWithProjection()(implicit ec: ExecutionContext): Enumerator[JsObject] = {
    val fSearchResults = client.execute(search).map(readHitsProjection)
    Enumerator.flatten(fSearchResults.map(res => Enumerator.enumerate(res)))
  }

  private def readHitsProjection(searchResponse: RichSearchResponse): mutable.ArraySeq[JsObject] = {
    searchResponse.hits.map(hit => {
      Json.parse(hit.sourceAsString).as[JsObject]
    })
  }

}
