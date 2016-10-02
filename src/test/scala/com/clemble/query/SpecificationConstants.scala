package com.clemble.query

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType.{IntegerType, StringType}
import com.sksamuel.elastic4s.{ElasticsearchClientUri, ElasticClient}
import reactivemongo.api.MongoDriver

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case object SpecificationConstants {

  val db = Await.result(MongoDriver().connection(List("localhost:27017")).database("test"), 1 minute)

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

}
