package com.clemble.query

import play.api.libs.json.{Json, JsObject, Format}
import reactivemongo.api.{MongoDriver}
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

  override val repo: MongoJSONSearchableRepository[Employee] with ProjectionSupport = new MongoJSONSearchableRepository[Employee] with MongoJSONProjectionSupport[Employee] {
    override val collection: JSONCollection = {
      SpecificationConstants.db.collection[JSONCollection]("employee_json")
    }
    override val queryTranslator: QueryTranslator[JsObject, JsObject] = new MongoJSONQueryTranslator()
    override implicit val f: Format[Employee] = format
  }

  override def save(employee: Employee): Boolean = {
    val fSave = repo.collection.update(Json.obj("_id" -> employee.name), Json.toJson(employee).as[JsObject], upsert = true)
    Await.result(fSave, 1 minute).errmsg.isEmpty
  }

  override def remove(employee: Employee): Boolean = {
    val fRemove = repo.collection.remove(Json.obj("name" -> employee.name))
    Await.result(fRemove, 1 minute).errmsg.isEmpty
  }

}
