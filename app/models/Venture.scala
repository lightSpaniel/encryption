package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import javax.inject._

import scala.concurrent.{Await, ExecutionContext, Future}


class VentureAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import slick.lifted.{Tag, TableQuery}

  class Ventures(tag: Tag) extends Table[VentureSchema](tag, "VENTURESCHEMA"){
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def name = column[String]("NAME")
    def projectClass = column[String]("PROJECTCLASS")
    def dataNonce = column[String]("DATANONCE")
    def dataValue = column[String]("DATAVALUE")
    def numberOfShares = column[Long]("NUMBEROFSHARES")
    override def * = (id, name, projectClass, dataNonce, dataValue, numberOfShares) <> (VentureSchema.tupled, VentureSchema.unapply)
  }

  val ventures = TableQuery[Ventures]
  //val dbConfig = dbConfigProvider.get[JdbcProfile]
  //val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def add(venture: VentureSchema): Future[String] = {
    dbConfig.db.run(ventures += venture).map(result => "VENTURE added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(ventures.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[VentureSchema]] = {
    dbConfig.db.run(ventures.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[VentureSchema]] = {
    dbConfig.db.run(ventures.result)
  }

  def updateNumberOfShares(newNumber: Long, id: Long): Future[String] = {
    dbConfig.db.run(ventures.filter(_.id === id).map(_.numberOfShares).update(newNumber)).map(result =>
      "numberOfShares updated").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

}





