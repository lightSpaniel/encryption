import play.api.data.Form
import play.api.data.Forms._

package object models {

  case class TestCC(bidNominal: Long, ventureId: Long, testString: String, key: Array[Byte])

  case class Sector(id: Long, name: String)
  case class Receipt(ventureId: Long, nominal: Long, price: Double)
  case class EncryptingUser(userName: String, passwordNonce: String, password: String)
  case class BasicVenture(name: String, description: String, price: Double)
  case class EncryptingVenture(sectorId: Long, profit: Double, turnover: Double, price: Double)
  case class BasicBid(amount: Double, bidType: String, nominal: Long)
  case class Price(name: String)
  case class Nominal(name: String)
  case class UserSession(id: Long,
                         userName: String,
                         password: String,
                         firstName: String,
                         lastName: String,
                         emailAddress: String,
                         companyIds: String,
                         sessionKey: String)

  case class Schema(id: Long, projectClass: String, dataNonce: String, dataValue: String)
  case class VentureSchema(id: Long,
                           name: String,
                           projectClass: String,
                           dataNonce: String,
                           dataValue: String,
                           numberOfShares: Long)
  case class EnhancedSchema(id: Long,
                            projectClass: String,
                            dataNonce: String,
                            dataValue: String,
                            userName: String,
                            passwordNonce: String,
                            passwordEncrypted: String,
                            sessionKey: String)

  sealed trait ProjectClass

  case class Bid(id: Long,
                 currency: String,
                 amount: Double,
                 bidType: String,
                 ventureId: Long,
                 nominal: Long) extends ProjectClass
  case class User(id: Long,
                  firstName: String,
                  lastName: String,
                  emailAddress: String,
                  companyIds: String) extends ProjectClass
  case class EnhancedUser(id: Long,
                          userName: String,
                          password: String,
                          firstName: String,
                          lastName: String,
                          emailAddress: String,
                          companyIds: String
                         )extends ProjectClass
  case class Venture(id: Long,
                     name: String,
                     sectorId: Long,
                     profit: Double,
                     turnover: Double,
                     price: Double,
                     numberOfShares: Long) extends ProjectClass
  ////////////
  case class BidFormData(amount: BigDecimal, bidType: String, nominal: Long)
  object BidForm {
    val form = Form(
      mapping(
        "amount" -> bigDecimal,
        "bidType" -> nonEmptyText,
        "nominal" -> longNumber
      )(BidFormData.apply)(BidFormData.unapply)
    )
  }
  case class UserFormData(firstName: String, lastName: String, emailAddress: String)
  object UserForm {
    val form = Form(
      mapping(
        "firstName" -> nonEmptyText(minLength = 1, maxLength = 50),
        "lastName" -> nonEmptyText(minLength = 1, maxLength = 50),
        "emailAddress" -> email
      )(UserFormData.apply)(UserFormData.unapply)
    )
  }
  case class getUserNameAndPasswordData(userName: String, password: String)
  object getIdForm{
    val form = Form(
      mapping(
        "userName" -> nonEmptyText,
        "password" -> nonEmptyText(minLength = 10, maxLength = 32)
      )(getUserNameAndPasswordData.apply)(getUserNameAndPasswordData.unapply)
    )
  }
  case class EnhancedUserData(userName: String,
                              password: String,
                              firstName: String,
                              lastName: String,
                              emailAddress: String)
  object EnhancedUserForm{
    val form = Form(mapping(
        "userName" -> nonEmptyText,
        "password" -> nonEmptyText(minLength = 10, maxLength = 32),
        "firstName" -> nonEmptyText(minLength = 1, maxLength = 50),
        "lastName" -> nonEmptyText(minLength = 1, maxLength = 50),
        "emailAddress" -> email
      )(EnhancedUserData.apply)(EnhancedUserData.unapply)
    )
  }
  case class VentureData(name: String,
                         sectorId: Long,
                         profit: BigDecimal,
                         turnover: BigDecimal,
                         price: BigDecimal,
                         numberOfShares: Long)
  object VentureForm{
    val form = Form(mapping(
      "name" -> nonEmptyText,
      "sectorId" -> longNumber,
      "profit" -> bigDecimal,
      "turnover" -> bigDecimal,
      "price" -> bigDecimal,
      "numberOfShares" -> longNumber
    )(VentureData.apply)(VentureData.unapply) )
  }

  case class UpdateUserFieldData(userField: String)
  object UpdateUserForm{
    val form = Form(mapping(
      "userField" -> nonEmptyText
    )(UpdateUserFieldData.apply)(UpdateUserFieldData.unapply)
    )
  }




}
