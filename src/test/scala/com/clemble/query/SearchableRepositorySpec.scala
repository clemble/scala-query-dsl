package com.clemble.query

import com.clemble.query.model._
import org.specs2.mutable.Specification
import org.specs2.specification.core.SpecificationStructure
import org.specs2.specification.create.FragmentsFactory
import org.specs2.specification.core
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Try

case class Employee(
                   name: String,
                   salary: Int
) extends Ordered[Employee] {
  override def compare(that: Employee): Int = name.compareTo(that.name)
}

// Following https://groups.google.com/forum/#!topic/specs2-users/6PEkpAzT080
trait BeforeAfterAllStopOnError extends SpecificationStructure with FragmentsFactory {
  def beforeAll
  def afterAll
  override def map(fs: => core.Fragments) = super.map(fs).
    prepend(
      fragmentFactory.step(beforeAll).stopOnError).
      append(fragmentFactory.step(afterAll)
    )
}

/**
  * Specification for abstract SearchableRepository
  */
trait SearchableRepositorySpec extends QuerySpecification {

  val repo: SearchableRepository[Employee] with ProjectionSupport

  override def readOne(query: Query): Option[Employee] = {
    val fEmployee = repo.findOne(query)
    Await.result(fEmployee, 1 minute)
  }

  override def readOneWithProjection(query: Query): Option[JsObject] = {
    val fEmployeeProj = repo.findOneWithProjection(query)
    val employeeProj = Await.result(fEmployeeProj, 1 minute)
    employeeProj
  }

  override def readAsList(query: Query): List[Employee] = {
    val fEmployees = repo.find(query) run Iteratee.fold(List.empty[Employee]) { (a, b) => b :: a }
    Await.result(fEmployees, 1 minute).reverse
  }

  def readAsListWithProjection(query: Query): List[JsObject] = {
    val fEmployees = repo.findWithProjection(query) run Iteratee.fold(List.empty[JsObject]){ (a, b) => b :: a }
    val employeesProj = Await.result(fEmployees, 1 minute).reverse
    employeesProj
  }

}

trait QueryFactorySpecification extends QuerySpecification {

  val queryFactory: QueryFactory[Employee]

  override def readAsList(query: Query): List[Employee] = {
    val fQuery = toQuery(query).find()
    val fEmployees = fQuery run Iteratee.fold(List.empty[Employee]){ (a, b) => b :: a }
    val employees = Await.result(fEmployees, 1 minute).reverse
    employees
  }

  override def readAsListWithProjection(query: Query): List[JsObject] = {
    val fQuery = toQuery(query).findWithProjection()
    val fEmployees = fQuery run Iteratee.fold(List.empty[JsObject]){ (a, b) => b :: a }
    val employees = Await.result(fEmployees, 1 minute).reverse
    employees
  }

  override def readOne(query: Query): Option[Employee] = {
    val fEmployee = toQuery(query).findOne()
    val employee = Await.result(fEmployee, 1 minute)
    employee
  }

  override def readOneWithProjection(query: Query): Option[JsObject] = {
    val fEmployee = toQuery(query).findOneWithProjection()
    val employee = Await.result(fEmployee, 1 minute)
    employee
  }

  private def toQuery(query: Query) = {
    queryFactory.
      create(query.where).
      sorted(query.sort).
      pagination(query.pagination).
      projection(query.projection)
  }

}

trait QuerySpecification extends Specification with BeforeAfterAllStopOnError {

  val employees = List(
    Employee("A", 100),
    Employee("B", 120),
    Employee("C", 130),
    Employee("D", 140),
    Employee("E", 150),
    Employee("F", 160)
  )

  def save(employee: Employee): Boolean

  def remove(employee: Employee): Boolean

  override def beforeAll(): Unit = {
    val savedAll = employees.map(employee => save(employee))
    require(savedAll.forall(_ == true), "Saved all employees")
    val canReadAll = (1 to 10).foldLeft(false)({ (agg, _) =>
      if (!agg) Thread.sleep(100)
      agg || readAsList(Query(Empty)).sorted == employees.sorted
    })
    require(canReadAll, "Read all Employees after save")
  }

  override def afterAll(): Unit = {
    employees.map(emp => Try(remove(emp)).getOrElse(false))
  }

  def readAsList(query: Query): List[Employee]

  def readAsListWithProjection(query: Query): List[JsObject]

  def readOne(query: Query): Option[Employee]

  def readOneWithProjection(query: Query): Option[JsObject]

  "empty query" should {

    "find all" in {
      val allReadEmployees = readAsList(Query(Empty))
      allReadEmployees.sorted shouldEqual employees.sorted
    }

  }

  "equals query" should {

    "find by name" in {
      val failedToSerialize = for {
        emp <- employees
      } yield {
        val empByName = readAsList(Query(Equals("name", emp.name)))
        if (empByName != List(emp)) Some(emp.name) else None
      }
      failedToSerialize.flatten aka "failed to find" shouldEqual Nil
    }

  }

  "not equals query" should {

    "exclude by name" in {
      for {
        emp <- employees
      } yield {
        val empByName = readAsList(Query(NotEquals("name", emp.name)))
        empByName should containTheSameElementsAs(employees.filterNot(_ == emp))
      }
    }

  }

  "projection query" should {

    val includeQuery = Query(Empty, sort = List(Ascending("name")), projection = List(Include("name")))
    val excludeQuery = Query(Empty, sort = List(Ascending("name")), projection = List(Exclude("name")))

    "include one" in {
      val employee = readOneWithProjection(includeQuery)
      employee shouldEqual Some(Json.obj("name" -> "A"))
    }

    "exclude one" in {
      val employeeProj = readOneWithProjection(excludeQuery)
      employeeProj shouldEqual Some(Json.obj("salary" -> 100))
    }

    "include as list" in {
      val employeesProj = readAsListWithProjection(includeQuery)
      employeesProj shouldEqual List(
        Json.obj("name" -> "A"),
        Json.obj("name" -> "B"),
        Json.obj("name" -> "C"),
        Json.obj("name" -> "D"),
        Json.obj("name" -> "E"),
        Json.obj("name" -> "F")
      )
    }

    "exclude as list" in {
      val employeesProj = readAsListWithProjection(excludeQuery)
      employeesProj shouldEqual List(
        Json.obj("salary" -> 100),
        Json.obj("salary" -> 120),
        Json.obj("salary" -> 130),
        Json.obj("salary" -> 140),
        Json.obj("salary" -> 150),
        Json.obj("salary" -> 160)
      )
    }

  }

  "sort query" should {

    "sort ASC" in {
      val firstSortedAsc = readOne(Query(Empty, sort = List(Ascending("name"))))
      firstSortedAsc shouldEqual Some(employees.head)
    }

    "sort DESC" in {
      val firstSortedDesc = readOne(Query(Empty, sort = List(Descending("name"))))
      firstSortedDesc shouldEqual Some(employees.last)
    }

  }

  "less operations" should {

    "less then 120 return only head" in {
      val onlyFirst = List(employees.head)
      val lessThenSecond = readAsList(Query(LessThen("salary", 120)))
      lessThenSecond shouldEqual onlyFirst
    }

    "less then equals 120 return first 2 elements and value" in {
      val firstAndLast = List(employees.head, employees.tail.head)
      val lessThenEqualsSecond = readAsList(Query(LessThenEquals("salary", 120)))
      lessThenEqualsSecond should containTheSameElementsAs(firstAndLast)
    }

  }

  "greater operations" should {

    "greater then 150 return only last" in {
      val onlyLast = List(employees.last)
      val greaterThenLast = readAsList(Query(GreaterThen("salary", 150)))
      greaterThenLast shouldEqual onlyLast
    }

    "greater then equals 150 return last and 1 before" in {
      val lastTwo = employees.reverse.take(2)
      val greaterThenLast = readAsList(Query(GreaterThenEquals("salary", 150)))
      greaterThenLast should containTheSameElementsAs(lastTwo)
    }

  }

  "Sort" should {

    "sort Ascending" in {
      val sortedAscending = readAsList(Query(Empty, sort = List(Ascending("name"))))
      sortedAscending shouldEqual employees
    }

    "sort Descending" in {
      val sortedDescending = readAsList(Query(Empty, sort = List(Descending("name"))))
      sortedDescending shouldEqual employees.reverse
    }

  }

  "Pagination" should {

    "Return only 1" in {
      val askForSingle = readAsList(Query(Empty, sort = List(Ascending("name")), pagination = PaginationParams(0, 1)))
      askForSingle shouldEqual List(employees.head)
    }

    "Return 2" in {
      val askForTwo = readAsList(Query(Empty, sort = List(Ascending("name")), pagination = PaginationParams(0, 2)))
      askForTwo should containTheSameElementsAs(employees.take(2))
    }

    "Read next page" in {
      val askForSingle = readAsList(Query(Empty, sort = List(Ascending("name")), pagination = PaginationParams(1, 1)))
      askForSingle shouldEqual List(employees(1))
    }

  }

  "And" should {

    "combine less then" in {
      val lessThen = readAsList(Query(And(LessThen("salary", 150), LessThen("salary", 110))))
      lessThen shouldEqual List(employees.head)
    }

    "combine greater then" in {
      val greaterThen = readAsList(Query(And(GreaterThen("salary", 150), GreaterThen("salary", 110))))
      greaterThen shouldEqual List(employees.last)
    }

    "take in between" in {
      val lessThen = readAsList(Query(And(LessThen("salary", 130), GreaterThen("salary", 100))))
      lessThen shouldEqual List(employees(1))
    }

  }

  "Or" should {

    "combine greater and less in" in {
      val firstAndLast = readAsList(Query(Or(GreaterThen("salary", 150), LessThen("salary", 110))))
      firstAndLast should containTheSameElementsAs(List(employees.head, employees.last))
    }

  }

}
