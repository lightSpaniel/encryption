package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import models._
import services.{Encrypt, Encryption}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

import scala.concurrent.{Await, Future}

@Singleton
class HomeController @Inject()(bidAccess: BidAccess,
                               userAccess: UserAccess,
                               ventureAccess: VentureAccess,
                               encryption: Encryption,
                               startEncryption: StartEncryption) extends Controller {

  private val key = startEncryption.secretKey

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  //Action async -> implicit request -> get list from model -> map -> result ->
  //val list = for(item <- result) yield decrypt(item, key, project class(string)) ->
  //pass list to view
  def listUsers = Action.async { implicit request =>
    userAccess.listAll map { result =>
      val decryptedSeq = for(item <- result) yield
        (Encrypt().decryptUser(item, key)).get

      Ok(views.html.listUser(decryptedSeq))
    }
  }

  def getAddUserForm = Action { implicit request =>
    Ok(views.html.inputUser(UserForm.form))
  }

  def addUser = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputUser(errorForm))),
      data => {
        val newUser = User(data.firstName, data.lastName, data.emailAddress, "empty")
        val jsonUser = Json.toJson(newUser)
        val encryptedUser = Encrypt().encrypt(jsonUser, key, "User")
        userAccess.add(encryptedUser).map(result =>
          Redirect(routes.HomeController.listUsers)
        )
      }
    )
  }




  def listAllUsers = Action.async{ implicit request =>
    userAccess.listAll map { result =>
      Ok(views.html.usersTest(result))
    }
  }

  def listAllVentures = Action.async{ implicit request =>
    ventureAccess.listAll map { result =>
      Ok(views.html.venturesTest(result))
    }
  }

  def listAllBids = Action.async{ implicit request =>
    bidAccess.listAll map { result =>
      Ok(views.html.bidsTest(result))
    }
  }

}
