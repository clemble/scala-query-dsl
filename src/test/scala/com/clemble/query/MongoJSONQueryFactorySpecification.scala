package com.clemble.query

import play.api.libs.json.{Format, JsObject, Json}
import reactivemongo.api.MongoDriver
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class MongoJSONQueryFactorySpecification extends QueryFactorySpecification{

  implicit val format = Json.format[Employee]

  private  val collection: JSONCollection = {
    val db = Await.result(MongoDriver().connection(List("localhost:27017")).database("test"), 1 minute)
    db.collection[JSONCollection]("employee_json_qf")
  }

  override val queryFactory: QueryFactory[Employee] = new MongoJSONQueryFactory(
    collection
  )

  override def save(employee: Employee): Boolean = {
    val fSave = collection.update(Json.obj("_id" -> employee.name), Json.toJson(employee).as[JsObject], upsert = true)
    val saveError = Await.result(fSave, 1 minute).errmsg.isEmpty
    saveError
  }

  override def remove(employee: Employee): Boolean = {
    val fRemove = collection.remove(Json.obj("name" -> employee.name))
    val removeError = Await.result(fRemove, 1 minute).errmsg.isEmpty
    removeError
  }

}
