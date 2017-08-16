import play.api.data.Form
import play.api.data.Forms._

package object models {

  case class Schema(id: Long, projectClass: String, dataNonce: String, dataValue: String)

  sealed trait ProjectClass

  case class Bid(currency: String,
                 amount: Double,
                 bidType: String) extends ProjectClass
  case class User(firstName: String,
                  lastName: String,
                  emailAddress: String,
                  companyIds: String) extends ProjectClass
  case class Venture(name: String,
                     sector: String,
                     profit: Double,
                     turnover: Double,
                     bidIds: String) extends ProjectClass

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























}
