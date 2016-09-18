package com.clemble.query

import com.clemble.query.model._
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import play.api.libs.iteratee.Iteratee

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

/**
  * Specification for abstract SearchableRepository
  */
trait SearchableRepositorySpec extends Specification with BeforeAfterAll {


  val employees = List(
    Employee("A", 100),
    Employee("B", 120),
    Employee("C", 130),
    Employee("D", 140),
    Employee("E", 150),
    Employee("F", 160)
  )

  val repo: SearchableRepository[Employee]


  def save(employee: Employee): Boolean
  def remove(employee: Employee): Boolean

  override def beforeAll(): Unit = {
    val saved = employees.map(emp => Try(save(emp)).getOrElse(false))
    saved.forall(_ == true) shouldEqual true
  }

  override def afterAll(): Unit = {
    val removed = employees.map(emp => Try(remove(emp)).getOrElse(false))
    removed.forall(_ == true) shouldEqual true
  }

  def readAsList(query: Query): List[Employee] = {
    val fEmployees = repo.find(query) run Iteratee.fold(List.empty[Employee]){ (a , b) => b :: a }
    Await.result(fEmployees, 1 minute).reverse
  }

  def readOne(query: Query): Option[Employee] = {
    val fEmployee = repo.findOne(query)
    Await.result(fEmployee, 1 minute)
  }

  "empty query" should {

    "find all" in {
      val allReadEmployees = readAsList(Query(Empty))
      allReadEmployees.sorted shouldEqual employees.sorted
    }


  }

  "equals query" should {

    "find by name" in {
      for {
        emp <- employees
      } yield {
        val empByName = readAsList(Query(Equals("name", emp.name)))
        empByName shouldEqual List(emp)
      }
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
