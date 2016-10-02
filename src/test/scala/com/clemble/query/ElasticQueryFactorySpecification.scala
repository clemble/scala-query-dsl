package com.clemble.query

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.mappings.FieldType.{IntegerType, StringType}
import com.sksamuel.elastic4s.source.JsonDocumentSource
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticQueryFactorySpecification extends QueryFactorySpecification {

  val indexAndType: IndexAndType = "test" / "employee"
  val client: ElasticClient = {
    val uri = ElasticsearchClientUri("localhost", 9300)
    val client = ElasticClient.transport(uri)
    initClient(client)
    client
  }
  private def initClient(client: ElasticClient) = {
    val needToRemove = client.execute(indexExists("test")).map(_.isExists()).await
    if (needToRemove) {
      require(client.execute(deleteIndex("test")).await().isAcknowledged())
    }

    val nameMapping = mapping("employee").fields(
      field("name", StringType).index("not_analyzed"),
      field("salary", IntegerType)
    )
    val createCommand = create index "test" mappings (nameMapping)

    val createResponse = client.execute(createCommand).await
    createResponse.isAcknowledged()
  }

  implicit val format: HitAs[Employee] = new HitAs[Employee] {
    override def as(hit: RichSearchHit): Employee = {
      val empJson = Json.parse(hit.sourceAsString)
      Employee(
        (empJson \ "name").as[String],
        (empJson \ "salary").as[Int]
      )
    }
  }

  override val queryFactory: QueryFactory[Employee] = new ElasticQueryFactory[Employee](client, indexAndType)

  override def remove(employee: Employee): Boolean = {
    val removeResponse = client.execute(delete id employee.name from indexAndType).await
    removeResponse.isFound()
  }

  override def save(employee: Employee): Boolean = {
    val empSource = JsonDocumentSource(s"""{"name": "${employee.name}", "salary": ${employee.salary}}""")
    val saveResponse = client.execute(indexInto(indexAndType) doc empSource id employee.name).await
    saveResponse.isCreated
  }

}
