package controllers

import javax.inject._

import play.api.mvc._
import models._
import services.{Encrypt, Encryption}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import scala.concurrent.Future

/**

  * @param ventureAccess - Slick access venture table
  * @param sectorAccess - Slick access sector table
  * @param controllerConstants - access to key and constants
  *
  * Dependecy injection is used where possible
  */

@Singleton
class HomeController @Inject()(ventureAccess: VentureAccess,
                               sectorAccess: SectorAccess,
                               ventureController: VentureController,
                               controllerConstants: ControllerConstants) extends Controller {
  /**
    * Main key - uses the application secret and converts
    * to a byte array as a means to encrypt and decrypt certain
    * data before passing to database/using
    */

  private val key = controllerConstants.universalPassword()

  /**
    * Homepage - serves simple view template
    */
  def index = Action {
    Ok(views.html.homePage(controllerConstants.welcome))
  }

  /**
    * Gets price for use in Ajax, converts to Json for use in Ajax call
    */
  def getPrice(id: Long) = Action.async { implicit request =>
    ventureController.getVentureWithId(id) map { result =>
      val price = Seq(Price(result.price.toString))
      Ok(Json.toJson(price))
    }
  }

  /**
    * Gets nominal for use in Ajax, converts to Json for use in Ajax call
    */
  def getNominal(id: Long) = Action.async { implicit request =>
    ventureController.getVentureWithId(id) map { result =>
      val nominal = Seq(Nominal(result.numberOfShares.toString))
      Ok(Json.toJson(nominal))
    }
  }

  /**
    * Gets sector for use in Ajax, converts to Json for use in Ajax call
    */
  def getSectorName(sectorId: Long) = Action.async { implicit request =>
    sectorAccess.get(sectorId) map { result =>
      val sector = Seq(SectorName(result.get.name))
      Ok(Json.toJson(sector))
    }
  }

  def getNewSessionVenture(sessionKey: String, venture: Venture): SessionVenture = {
        SessionVenture(venture.id,
          venture.name,
          venture.sectorId,
          venture.profit,
          venture.turnover,
          venture.price,
          venture.numberOfShares,
          sessionKey)
  }





}
