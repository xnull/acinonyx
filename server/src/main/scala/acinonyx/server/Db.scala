package acinonyx.server

import cats.effect.{ContextShift, IO}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext

object Lol extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", ""
  )
  val db = new Db()
  val create = db.createTable()
  val query = db.find("yay")

  create.run.transact(xa).unsafeRunSync()
  db.insert().run.transact(xa).unsafeRunSync()
  println(query.transact(xa).unsafeRunSync())
}

class Db {

  case class Country(code: String, name: String, population: Int)

  def insert() = {
    sql"insert into country(code, name, population) values ('555', 'yay', 123)".update
  }

  def createTable(): doobie.Update0 = {
    sql"create table country(code varchar, name varchar, population int)".update
  }

  def find(name: String): ConnectionIO[Option[Country]] = {
    sql"select code, name, population from country where name = $name".query[Country].option
  }
}
