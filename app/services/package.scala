import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._

package object services {


////////////
  implicit val encryptingVentureWrites = new Writes[EncryptingVenture]{
    def writes(encryptingVenture: EncryptingVenture) = Json.obj(
      "sectorId" -> encryptingVenture.sectorId,
      "profit" -> encryptingVenture.profit,
      "turnover" -> encryptingVenture.turnover,
      "price" -> encryptingVenture.price,
      "numberOfShares" -> encryptingVenture.numberOfShares
    )
  }

  implicit val readsEncryptingVenture: Reads[EncryptingVenture] = (
    (JsPath \ "sectorId").read[Long] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]
  )(EncryptingVenture.apply _)



  implicit val encryptingUserWrites = new Writes[EncryptingUser]{
   def writes(encryptingUser: EncryptingUser) = Json.obj(
     "userName" -> encryptingUser.userName,
     "passwordNonce" -> encryptingUser.passwordNonce,
     "password" -> encryptingUser.password
   )
  }

  implicit val readsEncryptingUser: Reads[EncryptingUser] = (
    (JsPath \ "userName").read[String] and
      (JsPath \ "passwordNonce").read[String] and
      (JsPath \ "password").read[String]
    )(EncryptingUser.apply _)

  ////////////

  implicit val enhancedUserWrites = new Writes[EnhancedUser]{
    def writes(user: EnhancedUser) = Json.obj(
      "id" -> user.id,
      "userName" -> user.userName,
      "password" -> user.password,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "emailAddress" -> user.emailAddress,
      "companyIds" -> user.companyIds
    )
  }

  implicit val readsEnhancedUser: Reads[EnhancedUser] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "userName").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "emailAddress").read[String] and
      (JsPath \ "companyIds").read[String]
    )(EnhancedUser.apply _)

  ////////////

  implicit val userWrites = new Writes[User]{
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "emailAddress" -> user.emailAddress,
      "companyIds" -> user.companyIds
    )
  }

  implicit val readsUser: Reads[User] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "emailAddress").read[String] and
      (JsPath \ "companyIds").read[String]
    )(User.apply _)

  ////////////

  implicit val bidWrites = new Writes[Bid]{
    def writes(bid: Bid) = Json.obj(
      "id" -> bid.id,
      "currency" -> bid.currency,
      "amount" -> bid.amount,
      "bidType" -> bid.bidType,
      "ventureId" -> bid.ventureId,
      "nominal" -> bid.nominal
    )
  }

  implicit val readsBid: Reads[Bid] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "currency").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "bidType").read[String] and
      (JsPath \ "ventureId").read[Long] and
      (JsPath \ "nominal").read[Long]
    )(Bid.apply _)

  ////////////

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
      (JsPath \ "sectorId").read[Long] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]
    )(Venture.apply _)

  ////////////
  def getEncryptingUserFromJson(json: JsValue): Option[EncryptingUser] = {
    json.validate[EncryptingUser] match {
      case success: JsSuccess[EncryptingUser] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }
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

  def getVentureFromJsonNew(json: JsValue): Option[EncryptingVenture] = {
    json.validate[EncryptingVenture] match {
      case success: JsSuccess[EncryptingVenture] => {
        Some(success.get)
      }
      case error: JsError => {
        println("Errors: " + JsError.toFlatForm(error).toString())
        None
      }
    }
  }

}

