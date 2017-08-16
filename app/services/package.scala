import models.{Bid, ProjectClass, User, Venture}
import play.api.libs.json._
import play.api.libs.functional.syntax._

package object services {

  implicit val userWrites = new Writes[User]{
    def writes(user: User) = Json.obj(
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "emailAddress" -> user.emailAddress,
      "companyIds" -> user.companyIds
    )
  }

  implicit val readsUser: Reads[User] = (
    (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "emailAddress").read[String] and
      (JsPath \ "companyIds").read[String]
    )(User.apply _)

  implicit val bidWrites = new Writes[Bid]{
    def writes(bid: Bid) = Json.obj(
      "currency" -> bid.currency,
      "amount" -> bid.amount,
      "bidType" -> bid.bidType
    )
  }

  implicit val readsBid: Reads[Bid] = (
    (JsPath \ "currency").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "bidType").read[String]
    )(Bid.apply _)

  implicit val ventureWrites = new Writes[Venture]{
    def writes(venture: Venture) = Json.obj(
      "name" -> venture.name,
      "sector" -> venture.sector,
      "profit" -> venture.profit,
      "turnover" -> venture.turnover,
      "bidIds" -> venture.bidIds
    )
  }

  implicit val readsVenture: Reads[Venture] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "sector").read[String] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "bidIds").read[String]
    )(Venture.apply _)


  def getUserFromJson(json: JsValue): Option[User] = {
    json.validate[User] match {
      case success: JsSuccess[User] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }
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

