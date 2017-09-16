import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._

package object services {

  implicit val enhancedUserWrites = new Writes[EnhancedUser]{
    def writes(user: EnhancedUser) = Json.obj(
      "id" -> user.id,
      "userName" -> user.userName,
      "password" -> user.password,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "emailAddress" -> user.emailAddress)}
  implicit val readsEnhancedUser: Reads[EnhancedUser] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "userName").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "emailAddress").read[String]
    )(EnhancedUser.apply _)

  def getEnhancedUserFromJson(json: JsValue): Option[EnhancedUser] = {
    json.validate[EnhancedUser] match {
      case success: JsSuccess[EnhancedUser] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }

  ////////////

  implicit val bidWrites = new Writes[Bid]{
    def writes(bid: Bid) = Json.obj(
      "id" -> bid.id,
      "currency" -> bid.currency,
      "amount" -> bid.amount,
      "bidType" -> bid.bidType,
      "ventureId" -> bid.ventureId,
      "userId" -> bid.userId,
      "nominal" -> bid.nominal)}
  implicit val readsBid: Reads[Bid] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "currency").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "bidType").read[String] and
      (JsPath \ "ventureId").read[Long] and
      (JsPath \ "userId").read[Long] and
      (JsPath \ "nominal").read[Double]
    )(Bid.apply _)

  def getBidFromJson(json: JsValue): Option[Bid] = {
    json.validate[Bid] match {
      case success: JsSuccess[Bid] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }

  ////////////////////

  implicit val ventureWrites = new Writes[Venture]{
    def writes(venture: Venture) = Json.obj(
      "id" -> venture.id,
      "name" -> venture.name,
      "sector" -> venture.sectorId,
      "profit" -> venture.profit,
      "turnover" -> venture.turnover,
      "price" -> venture.price,
      "numberOfShares" -> venture.numberOfShares
    )
  }
  implicit val readsVenture: Reads[Venture] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "sector").read[Long] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]
    )(Venture.apply _)

  def getVentureFromJson(json: JsValue): Option[Venture] = {
    json.validate[Venture] match {
      case success: JsSuccess[Venture] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }

}

