package com.clemble.query

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class MongoBSONQueryFactorySpecification extends QueryFactorySpecification {

  implicit val format: BSONDocumentReader[Employee] with BSONDocumentWriter[Employee] = new BSONDocumentReader[Employee] with BSONDocumentWriter[Employee] {
    override def write(t: Employee): BSONDocument = BSONDocument(
      "name" -> t.name,
      "salary" -> t.salary
    )

    override def read(bson: BSONDocument): Employee =
      Employee(
        bson.getAs[String]("name").get,
        bson.getAs[Int]("salary").get
      )
  }

  private val collection: BSONCollection = {
    SpecificationConstants.db.collection[BSONCollection]("employee_bson_qf")
  }

  override val queryFactory: QueryFactory[Employee] = new MongoBSONQueryFactory(collection)

  override def save(employee: Employee): Boolean = {
    val fSave = collection.update(BSONDocument("_id" -> employee.name), format.write(employee), upsert = true)
    Await.result(fSave, 1 minute).errmsg.isEmpty
  }

  override def remove(employee: Employee): Boolean = {
    val fRemove = collection.remove(BSONDocument("name" -> employee.name))
    Await.result(fRemove, 1 minute).errmsg.isEmpty
  }

}
