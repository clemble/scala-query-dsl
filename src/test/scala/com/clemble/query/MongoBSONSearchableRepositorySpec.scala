package com.clemble.query

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONDocument}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Await

/**
  * Mongo BSON SearchableRepository spec
  */
class MongoBSONSearchableRepositorySpec extends SearchableRepositorySpec {

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

  override val repo: MongoBSONSearchableRepository[Employee] with ProjectionSupport = new MongoBSONSearchableRepository[Employee] with MongoBSONProjectionSupport[Employee] {

    override val collection: BSONCollection = {
      val db = Await.result(MongoDriver().connection(List("localhost:27017")).database("test"), 1 minute)
      db.collection[BSONCollection]("employee")
    }

    override val queryTranslator: QueryTranslator[BSONDocument, BSONDocument] = new MongoBSONQueryTranslator()
    override implicit val f: BSONDocumentReader[Employee] = format

  }

  override def save(employee: Employee): Boolean = {
    val fSave = repo.collection.update(BSONDocument("_id" -> employee.name), format.write(employee), upsert = true)
    Await.result(fSave, 1 minute).errmsg.isEmpty
  }

  override def remove(employee: Employee): Boolean = {
    val fRemove = repo.collection.remove(BSONDocument("name" -> employee.name))
    Await.result(fRemove, 1 minute).errmsg.isEmpty
  }

}
