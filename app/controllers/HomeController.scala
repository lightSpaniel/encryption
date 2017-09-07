package controllers

import javax.inject._
import play.api.mvc._
import models._
import services.{Encrypt, Encryption}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import java.nio.charset.StandardCharsets


@Singleton
class HomeController @Inject()(bidAccess: BidAccess,
                               userAccess: UserAccess,
                               ventureAccess: VentureAccess,
                               sectorAccess: SectorAccess,
                               enhancedUserAccess: EnhancedUserAccess,
                               encryption: Encryption,
                               startEncryption: StartEncryption) extends Controller {

  private val key = startEncryption.universalPassword()


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  //Action async -> implicit request -> get list from model -> map -> result ->
  //val list = for(item <- result) yield decrypt(item, key, project class(string)) ->
  //pass list to view
  def listUsers = Action.async { implicit request =>
    userAccess.listAll map { result =>
      val decryptedSeq = for(item <- result) yield
        (Encrypt().decryptUser(item, key)).get

      Ok(views.html.listUser(decryptedSeq))
    }
  }

  def listEnhanced = Action.async { implicit request =>
    enhancedUserAccess.listAll map { result =>
      val decryptedSeq = for(item <- result) yield {
        userFromSchema(item)
      }
      Ok(views.html.listEnhanced(decryptedSeq))
    }

  }

  def getAddUserForm = Action { implicit request =>
    Ok(views.html.inputUser(UserForm.form))
  }

  def addUser = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputUser(errorForm))),
      data => {
        val newUser = User(0L, data.firstName, data.lastName, data.emailAddress, "empty")
        val jsonUser = Json.toJson(newUser)
        val encryptedUser = Encrypt().encrypt(jsonUser, key, "User")
        userAccess.add(encryptedUser).map(result =>
          Redirect(routes.HomeController.listUsers)
        )
      }
    )
  }

  def deleteUser(id: Long) = Action.async { implicit request =>
    userAccess.delete(id) map { result =>
      Redirect(routes.HomeController.loginForm)
    }
  }

  def getSeqOfUsers: Seq[User] = {
    val futureResult = userAccess.listAll map { result =>
      for(item <- result) yield
        (Encrypt().decryptUser(item, key)).get
    }
    Await.result(futureResult, 5.seconds)
  }


  def getSchemaByUsername(userName: String): EnhancedSchema = {
    val future = enhancedUserAccess.getWithUsername(userName)
    (Await.result(future, 5.seconds)).get
  }

  //get rid
  def checkPasswordReturnResult(inputPassword: String, candidateSchema: EnhancedSchema): Option[EnhancedUser] = {
    val newEncryptingUser = EncryptingUser(candidateSchema.userName,
      candidateSchema.passwordNonce,
      candidateSchema.passwordEncrypted)

    val candidatePasswordArray = Encrypt().decryptPassword(newEncryptingUser, key)
    val candidateFullPassword = new String(candidatePasswordArray, StandardCharsets.UTF_8)
    val candidatePassword = candidateFullPassword.substring(0,inputPassword.length)

    val checkResult = if(inputPassword == candidatePassword) {
      Encrypt().decryptEnhancedUser(candidateSchema, candidatePasswordArray)
    }else{
      None
    }
    checkResult
  }


  def getUser(id: Long) = Action.async { implicit request =>
    userAccess.get(id) map { result =>
      val decryptedItem = (Encrypt().decryptUser(result.get, key)).get
      Ok(views.html.singleUser(decryptedItem))
    }
  }

  //get rid
  def userFromSchema(enhancedSchema: EnhancedSchema): EnhancedUser = {
    val newEncryptingUser = EncryptingUser(enhancedSchema.userName,
      enhancedSchema.passwordNonce,
      enhancedSchema.passwordEncrypted)
    val userKey = Encrypt().decryptPassword(newEncryptingUser, key)
    (Encrypt().decryptEnhancedUser(enhancedSchema, userKey)).get
  }

  def getSchemaByUserName(userName: String): Option[EnhancedSchema] = {
    val futureResult = enhancedUserAccess.getWithUsername(userName)
    Await.result(futureResult, 5.seconds)
  }

  def updateSessionKey(enhancedSchema: EnhancedSchema): Option[EnhancedSchema] = {
    val newSessionKey = Encrypt().getSessionKey
    val futureResult = enhancedUserAccess.updateSessionKey(enhancedSchema.id, newSessionKey)

    Await.result(futureResult, 5.seconds) match {
      case "SessionKey updated" => {
        val futureSchema  = enhancedUserAccess.get(enhancedSchema.id)
        val schema = Await.result(futureSchema, 5.seconds)
        schema
      }
      case _ => None
    }
  }

  def getUserWithUserNameAndPassWord(userName: String, password: String): Future[UserSession] = {
    val checkSchema: Option[EnhancedSchema] = getSchemaByUserName(userName)
    checkSchema match {
      case None => Future(UserSession(0L, "", "", "", "", "", "", ""))
      case _ => {
        val candidateSchema = checkSchema.get
        val newEncryptingUser = EncryptingUser(candidateSchema.userName,
          candidateSchema.passwordNonce,
          candidateSchema.passwordEncrypted)
        val candidatePasswordArray = Encrypt().decryptPassword(newEncryptingUser, key)
        val candidateFullPassword = new String(candidatePasswordArray, StandardCharsets.UTF_8)
        val candidatePassword = candidateFullPassword.substring(0,password.length)

        if(candidatePassword == password){
          val newSchema = (updateSessionKey(candidateSchema)).get
          val user = (Encrypt().decryptEnhancedUser(newSchema, candidatePasswordArray)).get
          val usersession = UserSession(user.id,
            user.userName,
            user.password,
            user.firstName,
            user.lastName,
            user.emailAddress,
            user.companyIds,
            newSchema.sessionKey)
          Future(usersession)
        }else{
          Future(UserSession(0L, "", "", "", "", "", "", ""))
        }
      }
    }
  }

  def getEnhancedUserWithId(id: Long): EnhancedUser = {
    val futureEnhancedUser = enhancedUserAccess.get(id)
    val enhancedUserSchema = (Await.result(futureEnhancedUser, 5.seconds)).get
    val encryptingUser = EncryptingUser(enhancedUserSchema.userName,
      enhancedUserSchema.passwordNonce,
      enhancedUserSchema.passwordEncrypted)
    val userKey = Encrypt().decryptPassword(encryptingUser, key)

    (Encrypt().decryptEnhancedUser(enhancedUserSchema, userKey)).get
  }

  //add update user to accept session key
  def getUserWithSessionkey(sessionKey: String): Option[EnhancedUser] = {
      val futureSchema = enhancedUserAccess.getWithSessionKey(sessionKey)
      val schema = Await.result(futureSchema, 5.seconds)
      schema match{
        case None => None
        case _ => {
          val result = schema.get
          Some(userFromSchema(result))
        }
      }
  }


  def loginForm = Action { implicit request =>
    Ok(views.html.userNameAndPassword("Login", getIdForm.form))
  }

  def login = Action.async { implicit request =>
    getIdForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.userNameAndPassword("Login failed", errorForm))),
      data => {
        getUserWithUserNameAndPassWord(data.userName, data.password) map (res =>
          Ok(views.html.welcome(res.sessionKey))
          )
      })
  }

//////////
/////////
  def serveAddEnhancedUserForm = Action { implicit request =>
    Ok(views.html.inputEnhancedUser(EnhancedUserForm.form))
  }

  def getEnhancedSchema(enhancedUser: EnhancedUser): EnhancedSchema = {
    val salted = startEncryption.saltify(enhancedUser.password)
    val encryptingUser = Encrypt().encryptPassword(enhancedUser.userName, salted, key)
    val saltedPassword = startEncryption.convertPWStringToArrayBytes(salted)
    Encrypt().encryptEnhancedUser(enhancedUser, saltedPassword,encryptingUser)
  }


  //add function to search if username is taken already -> maybe check if you can do this with ajax
  def addEnhanced = Action.async { implicit request =>
    EnhancedUserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputEnhancedUser(errorForm))),
      data => {
        val newEnhancedUser = EnhancedUser(0L,
          data.userName,
          data.password,
          data.firstName,
          data.lastName,
          data.emailAddress,
          "empty")
        val enhancedSchema = getEnhancedSchema(newEnhancedUser)

        enhancedUserAccess.add(enhancedSchema).map(result =>
          Redirect(routes.HomeController.searchForm)
        )
      }
    )
  }

  //change redirect to login page ->
  def deleteEnhanced(id: Long) = Action.async{ implicit request =>
    enhancedUserAccess.delete(id) map { result =>
      Ok(views.html.userNameAndPassword("User deleted", getIdForm.form))
    }
  }

  def getEnhancedSchemaById(id: Long): EnhancedSchema = {
    val futureResult = enhancedUserAccess.get(id)
    (Await.result(futureResult, 5.seconds)).get
  }


  def updateItem(id: Long, field: String) = Action { implicit request =>
    val schema = getEnhancedSchemaById(id)
    val newEncryptingUser = EncryptingUser(schema.userName, schema.passwordNonce, schema.passwordEncrypted)
    val schemaKey = Encrypt().decryptPassword(newEncryptingUser, key)
    val enhancedUser = (Encrypt().decryptEnhancedUser(schema, schemaKey)).get
    Ok(views.html.updateItem(enhancedUser, field, UpdateUserForm.form))

  }

  def updateSpecific(field: String, update: String, enhancedUser: EnhancedUser): Option[EnhancedUser] = {
    field match{
      case "userName" => Some(EnhancedUser(0L,
        update,
        enhancedUser.password,
        enhancedUser.firstName,
        enhancedUser.lastName,
        enhancedUser.emailAddress,
        enhancedUser.companyIds))
      case "password" => Some(EnhancedUser(0L,
        enhancedUser.userName,
        update,
        enhancedUser.firstName,
        enhancedUser.lastName,
        enhancedUser.emailAddress,
        enhancedUser.companyIds))
      case "firstName" => Some(EnhancedUser(0L,
        enhancedUser.userName,
        enhancedUser.password,
        update,
        enhancedUser.lastName,
        enhancedUser.emailAddress,
        enhancedUser.companyIds))
      case "lastName"=> Some(EnhancedUser(0L,
        enhancedUser.userName,
        enhancedUser.password,
        enhancedUser.firstName,
        update,
        enhancedUser.emailAddress,
        enhancedUser.companyIds))
      case "emailAddress"=> Some(EnhancedUser(0L,
        enhancedUser.userName,
        enhancedUser.password,
        enhancedUser.firstName,
        enhancedUser.lastName,
        update,
        enhancedUser.companyIds))
      case "companyIds"=> Some(EnhancedUser(0L,
        enhancedUser.userName,
        enhancedUser.password,
        enhancedUser.firstName,
        enhancedUser.lastName,
        enhancedUser.emailAddress,
        update))
      case _ => None
    }
  }

  def justDelete(id: Long) = Action.async { implicit request =>
    enhancedUserAccess.delete(id) map ( result =>
      Redirect(routes.HomeController.searchForm)
    )
  }

  def performUpdateEnhancedUser(enhancedUser: EnhancedUser, update: String) = Action.async { implicit request =>
    UpdateUserForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.userNameAndPassword("Update failed", getIdForm.form))),
      data => {
        val newEnhancedUser = (updateSpecific(data.userField, update, enhancedUser)).get
        val enhancedSchema = getEnhancedSchema(newEnhancedUser)
        enhancedUserAccess.add(enhancedSchema).map(resultA =>
          Redirect(routes.HomeController.justDelete(enhancedUser.id))
        )
      }
    )
  }



 /////

  def serveNewVentureForm = Action { implicit request =>
    Ok(views.html.inputVenture(VentureForm.form, getSectors))
  }

  def getSectors: Seq[(String, String)] = {
    val futureResult = sectorAccess.listAll
    val sectors = Await.result(futureResult, 5.seconds)
    sectors map { sector =>
      sector.id.toString -> sector.name
    }
  }

  def addNewVenture = Action.async { implicit request =>
    VentureForm.form.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.inputVenture(VentureForm.form, getSectors))),
      data => {
          val newVenture = Venture(0L,
            data.name,
            data.sectorId.toLong,
            data.profit.toDouble,
            data.turnover.toDouble,
            data.price.toDouble,
            data.numberOfShares)
        val schema = Encrypt().encryptVenture(newVenture, key)
        ventureAccess.add(schema) map { result =>
          Ok(views.html.searchForm("Ventures"))
        }
  }
    )
  }

/**
  val abc = BasicVenture("abc", "hello")
  val xyz = BasicVenture("xyz", "world")
  val efg = BasicVenture("efg", "hello")
  val hij = BasicVenture("hij", "world")
  val klm = BasicVenture("klm", "hello")
  val nop = BasicVenture("nop", "world")

  val testBasicVentures = Seq(abc, xyz, efg, hij, klm, nop)

**/

  def seqOfVentures: Seq[Venture] = {
    val futureResult = ventureAccess.listAll
    val allVentureSchemas = Await.result(futureResult, 5.seconds)

    allVentureSchemas map { venture =>
      (Encrypt().decryptVentureNew(venture, key)).get
    }

  }

  def seqOfVentureNames: Seq[BasicVenture] = {
    val allVentures = seqOfVentures

    val allVentureNamesAsBasicVentures = allVentures map { venture =>
      BasicVenture(venture.name, "none", 0L)
    }

    allVentureNamesAsBasicVentures

  }

  def searchForm = Action {
    Ok(views.html.searchForm("Ventures"))
  }

  def updateSearch(string: String) = Action {
    //val newList1 = testBasicVentures.filter(_.name.toLowerCase.contains(string.toLowerCase))
    val newList = seqOfVentureNames.filter(_.name.toLowerCase.contains(string.toLowerCase))

    Ok(Json.toJson(newList))
  }

  /**

    Has to be in Akka (Fifo)



  def executeBid(sessionkey, ventureid) bid form {
    bidform error, data
    check sessionkey => if not go back login page
    adjustedbid = updateVentureWithBid(bid)

    bidaccess.add (change to return bid) map resultBid

    ok(view (   ) )
  }

    def updateVentureWithBid( bid ): bid {
        get future venture with venture id
        get venture with await
        check bidtype -> change venture nominal
        if totalbid is greater than nominal limit to nominal

  }


    **/


  def getVentureWithId(id: Long): Venture = {
    val futureVentureSchemas= ventureAccess.get(id)
    val ventureSchema = (Await.result(futureVentureSchemas, 5.seconds)).get
    (Encrypt().decryptVentureNew(ventureSchema, key)).get
  }

  def getPrice(id: Long) = Action { implicit request =>
    val venture = getVentureWithId(id)
    val price = Seq(Price(venture.price.toString))
    Ok(Json.toJson(price))
  }

  def getNominal(id: Long) = Action { implicit request =>
    val venture = getVentureWithId(id)
    val nominal = Seq(Nominal(venture.numberOfShares.toString))
    Ok(Json.toJson(nominal))
  }

  def testGetNominal = Action { implicit request =>
    val ventures = seqOfVentures
    val nominals = ventures map { venture =>
      Nominal(venture.numberOfShares.toString)
    }
    Ok(Json.toJson(nominals))
  }

  def singleUser(sessionKey: String) = Action { implicit request =>
    val userOption = getUserWithSessionkey(sessionKey)

    userOption match{
      case None => Ok(views.html.userNameAndPassword("Session expired", getIdForm.form))
      case _ =>
        val user = userOption.get
        val session = UserSession(user.id,
          user.userName,
          user.password,
          user.firstName,
          user.lastName,
          user.emailAddress,
          user.companyIds,
          sessionKey)
        Ok(views.html.singleEnhancedUser(session))
    }
  }

  def listVentures(sessionKey: String) = Action { implicit request =>
    val futureResult = enhancedUserAccess.getWithSessionKey(sessionKey)
    val result = Await.result(futureResult, 5.seconds)
    result match {
      case None => Ok(views.html.userNameAndPassword("Session key not recognised", getIdForm.form))
      case _ => {
        val ventures = seqOfVentures
        Ok(views.html.listVentures(ventures, sessionKey))
      }
    }
  }


/////////

  def bidOnVenture(ventureId: Long, sessionKey: String) = Action { implicit request =>
    checkIfSessionKeyIsValid(sessionKey) match {
      case false => Ok(views.html.userNameAndPassword("Session key not recognised", getIdForm.form))
      case true => {
        val futureVentureSchema = ventureAccess.get(ventureId)
        val ventureSchema = (Await.result(futureVentureSchema, 5.seconds)).get
        val venture = (Encrypt().decryptVentureNew(ventureSchema, key)).get

        val futureSector = sectorAccess.get(venture.sectorId)
        val sector = (Await.result(futureSector, 5.seconds)).get

        Ok(views.html.bidOnVenture(venture, sector.name, sessionKey, BidForm.form))
      }
    }
  }

  def checkIfSessionKeyIsValid(sessionKey: String): Boolean = {
    val futureResult = enhancedUserAccess.getWithSessionKey(sessionKey)
    val result = Await.result(futureResult, 5.seconds)

    result match{
      case None => false
      case _ => true
    }

  }


}
