package com.clemble.query

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.mappings.FieldType.{IntegerType, StringType}
import com.sksamuel.elastic4s.source.JsonDocumentSource
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticQueryFactorySpecification extends QueryFactorySpecification {

  val indexAndType: IndexAndType = "test" / "employee_qf"
  val client: ElasticClient = SpecificationConstants.client

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
