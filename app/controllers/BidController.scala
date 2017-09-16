package controllers

import javax.inject._
import play.api.mvc._
import models._
import services.{Encrypt, Encryption}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

@Singleton
class BidController @Inject()(bidAccess: BidAccess,
                              ventureController: VentureController,
                              userController: UserController,
                              encryption: Encryption,
                              sectorAccess: SectorAccess,
                              controllerConstants: ControllerConstants) extends Controller {

  /**
    * Main key - uses the application secret and converts
    * to a byte array as a means to encrypt and decrypt certain
    * data before passing to database/using
    */
  private val key = controllerConstants.universalPassword()

  def startBid(ventureId: Long, price: Double, sectorId: Long, sessionKey: String) = Action.async {
    implicit request=>

      val sector = getSectorNameWithId(sectorId)
      BidForm.form.bindFromRequest.fold(
        errorForm => Future.successful(Ok(views.html.homePage(controllerConstants.welcome))),
        data => {

          ventureController.updateNumberOfShares(data.nominal, ventureId) map { result =>
            result.success match {
              case "Success" => {
                val numberOfShares = result.numberOfShares
                val bidId = createBid(ventureId, userController.getUserSchemaWithSessionKey(sessionKey).id, price, "BUY", data.nominal, numberOfShares)
                Ok(views.html.displayReceipt(data.nominal, price, userController.getUserSchemaWithSessionKey(sessionKey).userName))
              }
              case _ => Ok(views.html.homePage(controllerConstants.welcome))
            }
          }
        }
      )
  }

  /**
    * Creates a bid, if successful uses part of the created record which is guarateed to be unique (nonce)
    * to get the record id (which is not known at creation)
    */
  def createBid(ventureId: Long, userId: Long, amount: Double, bidType: String, nominal: Long, oldBalance: Long): Option[Long] = {
    val bid = Bid(0L, "GBP", amount, bidType, ventureId, userId, nominal)
    val jsonBid = Json.toJson(bid)
    val schema = Encrypt().encrypt(jsonBid, key, "BID")
    val futureResult = bidAccess.add(schema)
    val result = Await.result(futureResult, 5.seconds)

    result match{
      case "Bid added" => {
        val futureBidId = bidAccess.getWithDataNonce(schema.dataNonce)
        val bidId = Await.result(futureBidId, 5.seconds).get.id
        Some(bidId)
      }
      case _ => {
        //If the bid update fails the application attempts to reverse the change
        //This is left to complete
        val future = ventureController.updateNumberOfShares(oldBalance, ventureId)
        None
      }
    }
  }

  /**
    * Uses Slick interface to get sector name with id
    */
  def getSectorNameWithId(sectorId: Long): String = {
    val future = sectorAccess.get(sectorId)
    Await.result(future, 5.seconds).get.name
  }

}
