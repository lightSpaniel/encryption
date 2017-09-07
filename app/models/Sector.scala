package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import javax.inject._

import com.google.inject.ImplementedBy
import controllers.HomeController
import play.api.Play

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}

class SectorAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import slick.lifted.{Tag, TableQuery}

  class Sectors(tag: Tag) extends Table[Sector](tag, "SECTOR") {
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def name = column[String]("NAME")
    override def * = (id, name)<>(Sector.tupled, Sector.unapply)
  }

  val sectors = TableQuery[Sectors]

  def add(sector: Sector): Future[String] = {
    dbConfig.db.run(sectors += sector).map(result => "Sector added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(sectors.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Sector]] = {
    dbConfig.db.run(sectors.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Sector]] = {
    dbConfig.db.run(sectors.result)
  }

}