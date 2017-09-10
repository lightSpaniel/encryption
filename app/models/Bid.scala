package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}


class BidAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import slick.lifted.{Tag, TableQuery}

  class Bids(tag: Tag) extends Table[Schema](tag, "BIDSCHEMA"){
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def projectClass = column[String]("PROJECTCLASS")
    def dataNonce = column[String]("DATANONCE")
    def dataValue = column[String]("DATAVALUE")
    override def * = (id, projectClass, dataNonce, dataValue) <> (Schema.tupled, Schema.unapply)
  }

  val bids = TableQuery[Bids]
  //val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  //val dbConfig = dbConfigProvider.get[JdbcProfile]

  def add(bid: Schema): Future[String] = {
    dbConfig.db.run(bids += bid).map(result => "Bid added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(bids.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Schema]] = {
    dbConfig.db.run(bids.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Schema]] = {
    dbConfig.db.run(bids.result)
  }

  def getWithDataNonce(dataNonce: String): Future[Option[Schema]] = {
    dbConfig.db.run(bids.filter(_.dataNonce === dataNonce).result.headOption) map { result =>
      result
    }
  }

}
