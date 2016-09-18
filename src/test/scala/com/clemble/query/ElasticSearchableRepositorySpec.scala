package com.clemble.query

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.source.{Indexable, JsonDocumentSource}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect._

class ElasticSearchableRepositorySpec extends SearchableRepositorySpec {

  def getManifest()(implicit manifest: Manifest[Employee]) = manifest

  override val repo: ElasticSearchableRepository[Employee] = new ElasticSearchableRepository[Employee] {
    override val queryTranslator: QueryTranslator[QueryDefinition, List[SortDefinition]] = new ElasticSearchQueryTranslator
    override val indexAndType: IndexAndType = "test" / "employee"
    override val client: ElasticClient = {
      val uri = ElasticsearchClientUri("localhost", 9300)
      val client = ElasticClient.transport(uri)
      client.execute(createIndex("test"))
      client
    }
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
    val fRemove = repo.client.execute(delete id employee.name from employee.name)
    val removeResponse = Await.result(fRemove, 1 minute)
    removeResponse.isFound()
  }

  override def save(employee: Employee): Boolean = {
    val empSource = JsonDocumentSource(s"""{"name": "${employee.name}", "salary": ${employee.salary}}""")
    val fSave = repo.client.execute(indexInto(repo.indexAndType) doc empSource id employee.name)
    val saveResponse = Await.result(fSave, 1 minute)
    saveResponse.isCreated
  }

}
