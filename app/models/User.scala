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


class UserAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import slick.lifted.{Tag, TableQuery}

  class UserSchemas(tag: Tag) extends Table[Schema](tag, "USERSCHEMA"){
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def projectClass = column[String]("PROJECTCLASS")
    def dataNonce = column[String]("DATANONCE")
    def dataValue = column[String]("DATAVALUE")
    override def * = (id, projectClass, dataNonce, dataValue) <>(Schema.tupled, Schema.unapply)
  }
  val userSchemas = TableQuery[UserSchemas]

  //val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def add(userSchema: Schema): Future[String] = {
    dbConfig.db.run(userSchemas += userSchema).map(result => "User added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(userSchemas.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[Schema]] = {
    dbConfig.db.run(userSchemas.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Schema]] = {
    dbConfig.db.run(userSchemas.result)
  }

  /**
  def updateName(id: Long, newName: String): Future[String] = {
    dbConfig.db.run(users.filter(_.id === id).map(_.name).update(newName)).map(result => "User added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }
    **/


}






