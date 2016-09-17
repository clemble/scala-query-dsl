package com.clemble.query

import play.api.libs.json.{Json, JsObject, Format}
import reactivemongo.api.{MongoDriver, MongoConnection, DB, DefaultDB}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.Await
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Mongo version of SearchableRepository
  */
class MongoJSONSearchableRepositorySpec extends SearchableRepositorySpec {

  implicit val format = Json.format[Employee]

  override val repo: MongoJSONSearchableRepository[Employee] = new MongoJSONSearchableRepository[Employee] {
    override val collection: JSONCollection = {
      val db = Await.result(MongoDriver().connection(List("localhost:27017")).database("test"), 1 minute)
      db.collection[JSONCollection]("employee")
    }
    override val queryTranslator: QueryTranslator[JsObject] = new MongoJSONQueryTranslator()
    override implicit val f: Format[Employee] = format
  }

  override def remove(employee: Employee): Unit = {
    val fRemove = repo.collection.remove(Json.obj("name" -> employee.name))
    Await.result(fRemove, 1 minute)
  }

  override def save(employee: Employee): Unit = {
    val fSave = repo.collection.update(Json.obj("_id" -> employee.name), Json.toJson(employee).as[JsObject], upsert = true)
    Await.result(fSave, 1 minute)
  }

}
