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

//tolmash
class EnhancedUserAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import slick.lifted.{Tag, TableQuery}

  class EnhancedUserSchemas(tag: Tag) extends Table[EnhancedSchema](tag, "ENHANCEDUSERSCHEMA"){
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def projectClass = column[String]("PROJECTCLASS")
    def dataNonce = column[String]("DATANONCE")
    def dataValue = column[String]("DATAVALUE")
    def userName = column[String]("USERNAME")
    def passwordNonce = column[String]("PASSWORDNONCE")
    def passwordEncrypted = column[String]("PASSWORDENCRYPTED")
    def sessionKey = column[String]("SESSIONKEY")
    override def * = (id,
      projectClass,
      dataNonce,
      dataValue,
      userName,
      passwordNonce,
      passwordEncrypted,
    sessionKey) <>(EnhancedSchema.tupled, EnhancedSchema.unapply)
  }
  val enhancedUserSchemas = TableQuery[EnhancedUserSchemas]

  //val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def add(enhancedUserSchema: EnhancedSchema): Future[String] = {
    dbConfig.db.run(enhancedUserSchemas += enhancedUserSchema).map(result => "User added").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(enhancedUserSchemas.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[EnhancedSchema]] = {
    dbConfig.db.run(enhancedUserSchemas.filter(_.id === id).result.headOption)
  }

  def getWithUsername(userName: String): Future[Option[EnhancedSchema]] = {
    dbConfig.db.run(enhancedUserSchemas.filter(_.userName === userName).result.headOption).map( result =>
      result
    )
  }
  def getWithSessionKey(sessionKey: String): Future[Option[EnhancedSchema]] = {
    dbConfig.db.run(enhancedUserSchemas.filter(_.sessionKey === sessionKey).result.headOption).map( result =>
      result
    )
  }

  def listAll: Future[Seq[EnhancedSchema]] = {
    dbConfig.db.run(enhancedUserSchemas.result)
  }

  def updateSessionKey(id: Long, newSessionKey: String): Future[String] = {
    dbConfig.db.run(enhancedUserSchemas.filter(_.id === id).map(_.sessionKey).update(newSessionKey)).map(result =>
      "SessionKey updated").recover{
      case ex: Exception => ex.getCause.getMessage
    }
  }



}







