package controllers

import javax.inject._

import play.api.mvc._
import models._
import services.{Encrypt, Encryption}
import scala.concurrent.ExecutionContext.Implicits.global
//This has been deprecated, but Play 2.5 doesn't yet have good support for
// 118n.Messages.Implicits which relies on Play.current.
//so we are staying with the deprecated Play.current
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import java.nio.charset.StandardCharsets

@Singleton
class UserController @Inject()(ventureController: VentureController,
                               enhancedUserAccess: EnhancedUserAccess,
                               encryption: Encryption,
                               controllerConstants: ControllerConstants) extends Controller {

  /**
    * Main key - uses the application secret and converts
    * to a byte array as a means to encrypt and decrypt certain
    * data before passing to database/using
    */

  private val key = controllerConstants.universalPassword()
  ////Add new user

  /**
    * Serves form to view template
    */
  def serveAddUserForm(message: String) = Action { implicit request =>
    Ok(views.html.inputEnhancedUser(EnhancedUserForm.form, message))
  }

  /**
    * Adds a new user using the Slick interface after check whether it already exists with the same username.
    */
  def addUser = Action.async { implicit request =>
    EnhancedUserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputEnhancedUser(errorForm, controllerConstants.transactionFailed))),
      data => {
        enhancedUserAccess.getWithUsername(data.userName)map { result =>
          result match{
            case None => {
              val newEnhancedUser = EnhancedUser(0L,
                data.userName,
                data.password,
                data.firstName,
                data.lastName,
                data.emailAddress)
              val enhancedSchema = getEnhancedSchema(newEnhancedUser)
              enhancedUserAccess.add(enhancedSchema)
              Redirect(routes.UserController.loginForm)
            }
            case _ => Redirect(routes.UserController.serveAddUserForm(controllerConstants.transactionFailed))
          }
        }
      }
    )
  }

  /// Login

  /**
    * Gets login form and passes to view template
    */
  def loginForm = Action { implicit request =>
    Ok(views.html.userNameAndPassword(controllerConstants.login, getIdForm.form))
  }

  /**
    * Reads login form makes you attempt to login again if you don't have valid credentials
    * If all works gets sessionkey and passes to welcome screen.
    */
  def login = Action.async { implicit request =>
    val ventures = ventureController.seqOfVentures
    getIdForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.userNameAndPassword(controllerConstants.loginFail, errorForm))),
      data => {
        getUserWithUserNameAndPassWord(data.userName, data.password) map (res =>
          res.sessionKey match{
            case "fail" => Ok(views.html.loginFailed(controllerConstants.loginFail))
            case _ => {
              Ok(views.html.welcome(ventures, res.sessionKey, BidForm.form))
            }
          }
          )
      })
  }


  /**
    * Checks whether sessionkey is valid if it is creates a user session and passes to view to update
    */
  def singleUser(sessionKey: String) = Action { implicit request =>
    val userOption = getUserWithSessionkey(sessionKey)

    userOption match {
      case None => Ok(views.html.userNameAndPassword(controllerConstants.sessionFail, getIdForm.form))
      case _ =>
        val user = userOption.get
        val session = UserSession(user.id,
          user.userName,
          user.password,
          user.firstName,
          user.lastName,
          user.emailAddress,
          sessionKey)
        Ok(views.html.singleEnhancedUser(session))
    }
  }

  /**
    * Uses Slick interface to access user and deletes
    */
  def deleteUser(id: Long) = Action.async { implicit request =>
    enhancedUserAccess.delete(id) map { result =>
      Redirect(routes.UserController.loginForm)
    }
  }



  /////Utilities - functions that aren't an Action


  /**
    * Takes the username and password and returns a future of a usersession (details needed for session)
    * This needs to return a future because this is used by a function which must take a future
    * (a function that reads data from a form must be an Action.async).
    *
    * Function checks whether a schema exists with the same username and whether the input password is the same
    * as the stored (and decrypted, desalted password)
    */
  def getUserWithUserNameAndPassWord(userName: String, password: String): Future[UserSession] = {
    //generate new sessionkey
    val newSessionKey = Encrypt().getSessionKey
    //Async check if schema exists
    getSchemaByUserName(userName) map { result =>
      result match {
        //If not return empty
        case None => UserSession(0L, "", "", "", "", "", "fail")
        case _ => {
          //get schema
          val candidateSchema = result.get
          //check password if schema found
          val newEncryptingUser = EncryptingUser(candidateSchema.userName,
            candidateSchema.passwordNonce,
            candidateSchema.passwordEncrypted)
          val candidatePasswordArray = Encrypt().decryptPassword(newEncryptingUser, key)
          val candidateFullPassword = new String(candidatePasswordArray, StandardCharsets.UTF_8)
          val candidatePassword = candidateFullPassword.substring(0, password.length)

          if (candidatePassword == password) {
            //if passwords match create new schema and update
            updateSessionKey(candidateSchema, newSessionKey)
            val user = Encrypt().decryptEnhancedUser(candidateSchema, candidatePasswordArray).get
            //Create new User Session
            val userSession = UserSession(user.id,
              user.userName,
              user.password,
              user.firstName,
              user.lastName,
              user.emailAddress,
              newSessionKey)
            userSession
          } else {
            //If no match return blank
            UserSession(0L, "", "", "", "", "", "fail")
          }
        }
      }
    }
  }

  /**
    * Accesses slick interface and gets user using username
    */
  def getSchemaByUserName(userName: String): Future[Option[EnhancedSchema]] = {
    enhancedUserAccess.getWithUsername(userName)
  }

  /**
    * Acceses Slick interface and encryption services to get a schema given a username
    */
  def getEnhancedSchema(enhancedUser: EnhancedUser): EnhancedSchema = {
    val salted = controllerConstants.saltify(enhancedUser.password)
    val encryptingUser = Encrypt().encryptPassword(enhancedUser.userName, salted, key)
    val saltedPassword = controllerConstants.convertPWStringToArrayBytes(salted)
    Encrypt().encryptEnhancedUser(enhancedUser, saltedPassword, encryptingUser)
  }

  /**
    * Accesses slick interface, decrypts and returns enhanced user
    */
  def getUserWithSessionkey(sessionKey: String): Option[EnhancedUser] = {
    val futureSchema = enhancedUserAccess.getWithSessionKey(sessionKey)
    val schema = Await.result(futureSchema, 5.seconds)
    schema match {
      case None => None
      case _ => {
        val result = schema.get
        userFromSchema(result)
      }
    }
  }

  def getUserSchemaWithSessionKey(sessionKey: String): EnhancedSchema = {
    val future = enhancedUserAccess.getWithSessionKey(sessionKey)
    Await.result(future, 5.seconds).get
  }

  /**
    * Rather than using a cookie or passing username and password around a one-time use
    * session key is generated each time the user logs in.
    * This needs to be generated and updated to the user record each time the user
    * logs in.
    */
  def updateSessionKey(enhancedSchema: EnhancedSchema, sessionKey: String): Unit = {
    enhancedUserAccess.updateSessionKey(enhancedSchema.id, sessionKey)
  }

  /**
    * Creates a case class to help encrypt user.
    * Uses case class and the main key get the exclusive key used to
    * encrypt the user data for that user only.
    */
  def userFromSchema(enhancedSchema: EnhancedSchema): Option[EnhancedUser] = {
    val newEncryptingUser = EncryptingUser(enhancedSchema.userName,
      enhancedSchema.passwordNonce,
      enhancedSchema.passwordEncrypted)
    val userKey = Encrypt().decryptPassword(newEncryptingUser, key)
    Encrypt().decryptEnhancedUser(enhancedSchema, userKey)
  }

}
