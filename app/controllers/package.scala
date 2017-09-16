import javax.inject.{Inject, Singleton}
import models._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

package object controllers {



  @Singleton
  class ControllerConstants @Inject()(configuration: play.api.Configuration) {

    /**
      * Accesess the app secret key
      */
    private val secret = configuration.getString("play.crypto.secret").get

    /**
      * Turns the main key key into a byte array to be used by Kalium AES-256 bit encryption
      */
    def secretKey: Array[Byte] = {
      (for(c <- secret) yield c.toByte).toArray
    }

    /**
      * Salts a password (for example taken from a form input) and salts it (adds additional characters)
      * so that it has a uniform 32 characters for use by Kalium AES-256 bit encryption as a string key
      */
    def saltify(password: String): String = {
      val saltBit = secret.slice(password.length, 32)
      password ++ saltBit
    }

    /**
      * Converts password (string) into array bytes
      */
    def convertPWStringToArrayBytes(passwordString: String): Array[Byte] = {
      (for(c<-passwordString) yield c.toByte).toArray
    }

    /**
      * Converts a string into a salted byte array
      */
    def saltPassword(password: String): Array[Byte] = {
      val userPassword = {
        for(char <- password.toCharArray) yield char.toByte
      }
      val salt = secretKey.slice(userPassword.length, 32)
      userPassword ++ salt
    }

    //WOULD THIS WORK WITH ANY OTHER PASSWORD?

    /**
      *
      */
    def universalPassword(password: String = secret): Array[Byte] = {
      val userPassword = {
        for(char <- password.toCharArray) yield char.toByte
      }
      val salt = secretKey.slice(userPassword.length, 32)
      userPassword ++ salt
    }

    //String constants
    val login = "Login to application"
    val loginFail = "Failed to login"
    val transactionComplete = "Transaction complete"
    val transactionFailed = "Transaction failed"
    val sessionFail = "Session expired"
    val welcome = "Welcome to the application"

  }
  implicit val readsPrice = Reads[Price] _
  implicit val priceWrites = new Writes[Price]{
    def writes(price: Price) = Json.obj(
      "name" -> price.name
    )
  }

  implicit val sectorNamePrice = Reads[SectorName] _
  implicit val sectorNameWrites = new Writes[SectorName]{
    def writes(sectorName: SectorName) = Json.obj(
      "name" -> sectorName.name
    )
  }

  implicit val nominalReads = Reads[Nominal] _
  implicit val nominalWrites = new Writes[Nominal]{
    def writes(nominal: Nominal) = Json.obj(
      "name" -> nominal.name
    )
  }

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

  implicit val ventureWrites = new Writes[Venture]{
    def writes(venture: Venture) = Json.obj(
      "id" -> venture.id,
      "name" -> venture.name,
      "sector" -> venture.sectorId,
      "profit" -> venture.profit,
      "turnover" -> venture.turnover,
      "price" -> venture.price,
      "numberOfShares" -> venture.numberOfShares)}
  implicit val readsVenture: Reads[Venture] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "name").read[String] and
      (JsPath \ "sectorId").read[Long] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]
    )(Venture.apply _)

}
