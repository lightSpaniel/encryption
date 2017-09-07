import javax.inject.{Inject, Singleton}
import java.security.SecureRandom

import com.google.inject.ImplementedBy
import models._
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

    def saltify(password: String): String = {
      val saltBit = secret.slice(password.length, 32)
      password ++ saltBit
    }

    def convertPWStringToArrayBytes(passwordString: String): Array[Byte] = {
      (for(c<-passwordString) yield c.toByte).toArray
    }

    def saltPassword(password: String): Array[Byte] = {
      val userPassword = {
        for(char <- password.toCharArray) yield char.toByte
      }
      val salt = secretKey.slice(userPassword.length, 32)
      userPassword ++ salt
    }

    def universalPassword(password: String = secret): Array[Byte] = {
      val userPassword = {
        for(char <- password.toCharArray) yield char.toByte
      }
      val salt = secretKey.slice(userPassword.length, 32)
      userPassword ++ salt
    }

  }
  implicit val priceWrites = new Writes[Price]{
    def writes(price: Price) = Json.obj(
      "name" -> price.name
    )
  }

  implicit val readsPrice = Reads[Price] _


  implicit val sectorWrites = new Writes[Sector]{
    def writes(sec: Sector) = Json.obj(
      "id" -> sec.id,
      "name" -> sec.name
    )
  }

  implicit val readsSector: Reads[Sector] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String]
  )(Sector.apply _)

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

  implicit val basicVentureWrites = new Writes[BasicVenture]{
    def writes(basicVenture: BasicVenture) = Json.obj(
      "name" -> basicVenture.name,
      "description" -> basicVenture.description
    )
  }
  implicit val basicVentureReads = Reads[BasicVenture] _

  implicit val nominalWrites = new Writes[Nominal]{
    def writes(nominal: Nominal) = Json.obj(
      "name" -> nominal.name
    )
  }

  implicit val nominalReads = Reads[Nominal] _

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

  implicit val userPasswordWrites = new Writes[getUserNameAndPasswordData]{
    def writes(userPassword: getUserNameAndPasswordData) = Json.obj(
      "userName" -> userPassword.userName,
      "password" -> userPassword.password
    )
  }

  implicit val readsUserPassword: Reads[getUserNameAndPasswordData] = (
    (JsPath \ "userName").read[String] and
      (JsPath \ "password").read[String]
  )(getUserNameAndPasswordData.apply _)

  implicit val readsTestEnhancedSchema = new Writes[EnhancedSchema]{
    def writes(enhancedSchema: EnhancedSchema) = Json.obj(
      "id" -> enhancedSchema.id,
      "projectClass" -> enhancedSchema.projectClass,
      "dataNonce" -> enhancedSchema.dataNonce,
      "dataValue" -> enhancedSchema.dataValue,
      "userName" -> enhancedSchema.userName,
      "passwordNonce" -> enhancedSchema.passwordNonce,
      "passwordEncrypted" -> enhancedSchema.passwordEncrypted,
      "sessionKey" -> enhancedSchema.sessionKey)
  }

  implicit val testEnhancedSchema: Reads[EnhancedSchema] =(
      (JsPath \ "id").read[Long] and
        (JsPath \ "projectClass").read[String] and
        (JsPath \ "dataNonce").read[String] and
        (JsPath \ "dataValue").read[String] and
        (JsPath \ "userName").read[String] and
        (JsPath \ "passwordNonce").read[String] and
        (JsPath \ "passwordEncrypted").read[String] and
        (JsPath \ "sessionKey").read[String]
    )(EnhancedSchema.apply _)

}
