package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import javax.inject._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

import services.{Encrypt, Encryption}

class VentureAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import slick.lifted.{Tag, TableQuery}

  class Ventures(tag: Tag) extends Table[Schema](tag, "VENTURESCHEMA"){
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def projectClass = column[String]("PROJECTCLASS")
    def dataNonce = column[String]("DATANONCE")
    def dataValue = column[String]("DATAVALUE")
    override def * = (id, projectClass, dataNonce, dataValue) <> (Schema.tupled, Schema.unapply)
  }

  val ventures = TableQuery[Ventures]
  //val dbConfig = dbConfigProvider.get[JdbcProfile]
  //val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def add(venture: Schema): Future[String] = {
    dbConfig.db.run(ventures += venture).map(result => "Success").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(ventures.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Schema]] = {
    dbConfig.db.run(ventures.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Schema]] = {
    dbConfig.db.run(ventures.result)
  }

}





