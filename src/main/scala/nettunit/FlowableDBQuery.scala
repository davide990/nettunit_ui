package nettunit

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import cats.effect.unsafe.implicits.global

case class FlowableProcessDefRecord(id: String, rev: Int, name: String, key: String, version: String, deployment_id: String, tenant_id: String)

object FlowableDBQuery {

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/nettunit", "dguastel", "dguastel"
  )

  def findAllProcessDef(): List[FlowableProcessDefRecord] = {
    sql"select id_,rev,name_,key_,version_,deployment_id_, tenant_id_ from public.act_re_procdef"
      .query[FlowableProcessDefRecord]
      .to[List]
      .transact(xa) // IO[List[FlowableProcessDefRecord]]
      .unsafeRunSync() // List[FlowableProcessDefRecord]
  }

  case class Country(code: String, name: String, population: Long)

  def find(n: String): ConnectionIO[Option[Country]] =
    sql"select code, name, population from country where name = $n".query[Country].option

  def findAll(): Unit = {
    sql"select name from country"
      .query[String] // Query0[String]
      .to[List] // ConnectionIO[List[String]]
      .transact(xa) // IO[List[String]]
      .unsafeRunSync() // List[String]
      .take(5) // List[String]
      .foreach(println) // Unit
    // Afghanistan
    // Netherlands
    // Netherlands Antilles
    // Albania
    // Algeria
  }

}
