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

import play.api.libs.json.Json
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


@Singleton
class VentureController @Inject()(ventureAccess: VentureAccess,
                                  encryption: Encryption,
                                  sectorAccess: SectorAccess,
                                  controllerConstants: ControllerConstants) extends Controller {

  /**
    * Main key - uses the application secret and converts
    * to a byte array as a means to encrypt and decrypt certain
    * data before passing to database/using
    */
  private val key = controllerConstants.universalPassword()

  /**
    * Serves form to view
    */
  def serveNewVentureForm = Action { implicit request =>
    Ok(views.html.inputVenture("Add new venture", VentureForm.form, getSectors))
  }

  /**
    * Uses form data to build and then save a new venture
    */
  def addVenture = Action.async { implicit request =>
    VentureForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputVenture("Error in form input", VentureForm.form, getSectors))),
      data => {
        //Check if the venture name is unique, if not deny
        if (checkVentureNameIsUnique(data.name)){
          val newVenture = Venture(0L,
            data.name,
            data.sectorId,
            data.profit.toDouble,
            data.turnover.toDouble,
            data.price.toDouble,
            data.numberOfShares)
          val jsonVenture = Json.toJson(newVenture)
          val schema = Encrypt().encrypt(jsonVenture, key, "VENTURE")
          ventureAccess.add(schema) map { result =>
            Ok(views.html.userNameAndPassword(controllerConstants.login, getIdForm.form))
          }
        }else{
          Future.successful(Ok(views.html.inputVenture("Venture name already taken, try another", VentureForm.form, getSectors)))
        }
      }
    )
  }

  def singleVenture(ventureId: Long) = Action.async { implicit request =>
    ventureAccess.get(ventureId) map { result =>
      val venture = Encrypt().decryptVenture(result.get, key)
      Ok(views.html.singleVenture(venture.get))
    }
  }
  //Utilities

  def checkVentureNameIsUnique(name: String): Boolean = {
    seqOfVentures.filter(_.name == name).length match {
      case 0 => true
      case _ => false
    }
  }
  /**
    * Uses slick interface to access sector data and converts into
    * sequence for use in view selector
    */
  val getSectors: Seq[(String, String)] = {
    val futureResult = sectorAccess.listAll
    val sectors = Await.result(futureResult, 5.seconds)
    sectors map { sector =>
      sector.id.toString -> sector.name
    }
  }

  /**
    * Accesses Slick interface and gets result of future
    */
  def getVentureWithId(id: Long): Future[Venture] = {
    ventureAccess.get(id) map { result =>
      Encrypt().decryptVenture(result.get, key).get
    }
  }


  /**
    * Gets sequence of ventures for use in view templates
    */
  def seqOfVentures: Seq[Venture] = {
    val futureResult = ventureAccess.listAll
    val allVentureSchemas = Await.result(futureResult, 5.seconds)

    allVentureSchemas map { venture =>
      Encrypt().decryptVenture(venture, key).get
    }
  }


  def updateNumberOfShares(purchaseNominal: Long, ventureId: Long): Future[resultOfUpdate] = {
    ventureAccess.get(ventureId) map { result =>
      val venture = Encrypt().decryptVenture(result.get, key).get
      val newNominal = venture.numberOfShares - purchaseNominal
      val newVenture = Venture(0L,
        venture.name,
        venture.sectorId,
        venture.profit,
        venture.turnover,
        venture.price,
        newNominal)

      val jsonVenture = Json.toJson(newVenture)
      val newSchema = Encrypt().encrypt(jsonVenture, key, "VENTURE")
      val addNew = ventureAccess.add(newSchema)
      val success = Await.result(addNew, 5.seconds)
      val updateResult = resultOfUpdate(success, venture.numberOfShares)
      updateResult
    }
  }

}
