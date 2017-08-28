import javax.inject.{Inject, Singleton}
import java.security.SecureRandom

import com.google.inject.ImplementedBy
import models.{Bid, User, Venture, BasicVenture}
import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

package object controllers {

  @Singleton
  class StartEncryption @Inject()(configuration: play.api.Configuration) {

    private val secret = configuration.getString("play.crypto.secret").get

    def secretKey: Array[Byte] = {
      (for(c <- secret) yield c.toByte).toArray
    }

  }

  implicit val basicVentureWrites = new Writes[BasicVenture]{
    def writes(basicVenture: BasicVenture) = Json.obj(
      "name" -> basicVenture.name,
      "description" -> basicVenture.description
    )
  }

  implicit val basicVentureReads = Reads[BasicVenture] _

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

}
