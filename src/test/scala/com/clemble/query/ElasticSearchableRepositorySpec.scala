package com.clemble.query

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.source.{JsonDocumentSource}
import play.api.libs.json.Json

class ElasticSearchableRepositorySpec extends SearchableRepositorySpec {

  override val repo: ElasticSearchableRepository[Employee] with ProjectionSupport = new ElasticSearchableRepositoryWithProjectionSupport[Employee] {
    override val queryTranslator: QueryTranslator[QueryDefinition, List[SortDefinition]] = new ElasticSearchQueryTranslator
    override val indexAndType: IndexAndType = "test" / "employee"
    override val client: ElasticClient = SpecificationConstants.client

    override implicit val format: HitAs[Employee] = new HitAs[Employee] {
      override def as(hit: RichSearchHit): Employee = {
        val empJson = Json.parse(hit.sourceAsString)
        Employee(
          (empJson \ "name").as[String],
          (empJson \ "salary").as[Int]
        )
      }
    }
  }


  override def remove(employee: Employee): Boolean = {
    val removeResponse = repo.client.execute(delete id employee.name from repo.indexAndType).await
    removeResponse.isFound()
  }

  override def save(employee: Employee): Boolean = {
    val empSource = JsonDocumentSource(s"""{"name": "${employee.name}", "salary": ${employee.salary}}""")
    val saveResponse = repo.client.execute(indexInto(repo.indexAndType) doc empSource id employee.name).await
    saveResponse.isCreated
  }

}
