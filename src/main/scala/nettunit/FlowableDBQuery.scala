package nettunit

import cats.effect._
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import shapeless._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import doobie.util.stream

import java.sql.Timestamp

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import cats.effect.unsafe.implicits.global

//case class FlowableProcessDefRecord(id: String, rev: Int, name: String, key: String, version: String, deployment_id: String, tenant_id: String)

//https://www.scalafx.org/docs/faq_TableView_with_Custom_cell/
case class FlowableProcessDefRecord(id_ : String,
                                    rev_ : Int,
                                    name_ : String,
                                    key_ : String,
                                    version_ : String,
                                    deployment_id_ : String,
                                    tenant_id_ : String) {
  val id = new StringProperty(this, "id", id_)
  val rev = new ObjectProperty(this, "rev", rev_)
  val name = new StringProperty(this, "name", name_)
  val key = new StringProperty(this, "key", key_)
  val version = new StringProperty(this, "version", version_)
  val deployment_id = new StringProperty(this, "deployment_id", deployment_id_)
  val tenant_id = new StringProperty(this, "tenant_id", tenant_id_)
}

//String :: String :: Int :: Option[Double] :: HNil
//String::Int::String::String::Timestamp::Timestamp::Int:Option[String]::String::Option[String]::Option[String]
case class FlowableProcessInstanceHistoricRecord(id_ : String,
                                                 rev_ : Int,
                                                 proc_inst_id_ : String,
                                                 proc_def_id_ : String,
                                                 start_time_ : Timestamp,
                                                 end_time_ : Timestamp,
                                                 duration_ : Int,
                                                 start_user_id_ : Option[String],
                                                 start_act_id_ : String,
                                                 end_act_id_ : Option[String],
                                                 delete_reason_ : Option[String]) {
  val id = new StringProperty(this, "id", id_)
  val rev = new ObjectProperty(this, "rev", rev_)
  val proc_inst_id = new StringProperty(this, "proc_inst_id", proc_inst_id_)
  val proc_def_id = new StringProperty(this, "proc_def_id", proc_def_id_)
  val start_time = new ObjectProperty(this, "start_time", start_time_)
  val end_time = new ObjectProperty(this, "end_time", end_time_)
  val duration = new ObjectProperty(this, "duration", duration_)
  val start_user_id = new StringProperty(this, "start_user_id", start_user_id_.getOrElse(""))
  val start_act_id = new StringProperty(this, "start_act_id", start_act_id_)
  val end_act_id = new StringProperty(this, "end_act_id", end_act_id_.getOrElse(""))
  val delete_reason = new StringProperty(this, "delete_reason", delete_reason_.getOrElse(""))
}

case class FlowableTaskInstHistoricRecord(id_ : String,
                                          rev_ : Int,
                                          proc_def_id_ : String,
                                          task_def_key_ : String,
                                          proc_inst_id_ : String,
                                          name_ : String,
                                          start_time_ : Timestamp,
                                          end_time_ : Timestamp,
                                          duration_ : Int,
                                          delete_reason_ : Option[String],
                                          priority_ : Int,
                                          last_updated_time_ : Timestamp) {
  val id = new StringProperty(this, "id", id_)
  val rev = new ObjectProperty(this, "rev", rev_)
  val proc_def_id = new StringProperty(this, "proc_def_id", proc_def_id_)
  val task_def_key = new StringProperty(this, "task_def_key", task_def_key_)
  val proc_inst_id = new StringProperty(this, "proc_inst_id", proc_inst_id_)
  val name = new StringProperty(this, "name", name_)
  val start_time = new ObjectProperty(this, "start_time", start_time_)
  val end_time = new ObjectProperty(this, "end_time", end_time_)
  val duration = new ObjectProperty(this, "duration", duration_)
  val delete_reason = new StringProperty(this, "delete_reason", delete_reason_.getOrElse(""))
  val priority = new ObjectProperty(this, "priority", priority_)
  val last_updated_time = new ObjectProperty(this, "last_updated_time", last_updated_time_)


}

object FlowableDBQuery {

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/nettunit", "dguastel", "dguastel"
  )

  def findAllProcessInstancesHistoric(): List[FlowableProcessInstanceHistoricRecord] = {
    sql"select id_,rev_,proc_inst_id_,proc_def_id_,start_time_,end_time_,duration_,start_user_id_,start_act_id_,end_act_id_,delete_reason_ from public.act_hi_procinst"
      //sql"select id_,rev_,proc_inst_id_,proc_def_id_ from public.act_hi_procinst"
      .query[FlowableProcessInstanceHistoricRecord]
      //.query[String::Int::String::String::Timestamp::Timestamp::Int:Option[String]::String::Option[String]::Option[String]]
      .to[List]
      .transact(xa) // IO[List[FlowableProcessInstanceHistoricRecord]]
      .unsafeRunSync() // List[FlowableProcessInstanceHistoricRecord]
  }

  def findAllProcessDef(): List[FlowableProcessDefRecord] = {
    sql"select id_,rev_,name_,key_,version_,deployment_id_, tenant_id_ from public.act_re_procdef"
      .query[FlowableProcessDefRecord]
      .to[List]
      .transact(xa) // IO[List[FlowableProcessDefRecord]]
      .unsafeRunSync() // List[FlowableProcessDefRecord]
  }

  def findAllTaskInstHistoric(): List[FlowableTaskInstHistoricRecord] = {
    sql"select id_,rev_,proc_def_id_,task_def_key_,proc_inst_id_,name_,start_time_,end_time_,duration_,delete_reason_,priority_,last_updated_time_ from public.act_hi_taskinst"
      .query[FlowableTaskInstHistoricRecord]
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
