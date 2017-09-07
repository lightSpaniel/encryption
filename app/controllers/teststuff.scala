package controllers

import java.nio.charset.StandardCharsets

import models._
import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
import org.abstractj.kalium.crypto.Random
import play.api.libs.json._
import services._
import play.api.libs.functional.syntax._

import scala.concurrent.{Await, Future}

object TestNonce{
  private val random = new Random()

  def createNonce(): Nonce = {
    new Nonce(random.randomBytes(CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES))
  }

  def nonceFromBytes(data: Array[Byte]): Nonce = {
    if (data == null || data.length != CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES) {
      throw new IllegalArgumentException("This nonce has an invalid size: " + data.length)
    }
    new Nonce(data)
  }
}

object teststuff extends App{

  //val testschema = add(form)

  val testsecret = "8tinAB6qnWnKMnbgT6khJLcxDdbBSjfR"
  def testsecretKey: Array[Byte] = {
    (for(c <- testsecret) yield c.toByte).toArray
  }
  def saltify(password: String): String = {
    val saltBit = testsecret.slice(password.length, 32)
    password ++ saltBit
  }
  def convertPWStringToArrayBytes(passwordString: String): Array[Byte] = {
    (for(c<-passwordString) yield c.toByte).toArray
  }

  //val pw = "ruba9Grake"
  //val sw = saltify(pw)

  //println("length: " + sw.length + "pw: " + sw)

  def testsaltPassword(password: String): Array[Byte] = {
    val userPassword = {
      for(char <- password.toCharArray) yield char.toByte
    }
    val salt = testsecretKey.slice(userPassword.length, 32)
    userPassword ++ salt
  }

  private val key = testsecretKey


  case class formdata(user: String,
                      password: String,
                      firstname: String,
                      lastName: String,
                      email: String)

  val form = formdata("jsmith28",
    "ruba9Grake",
    "Jeremy",
    "Smith",
    "j3r3my5m1th82@gmail.com")

  def add(form: formdata): EnhancedSchema = {
    val newEnhancedUser = EnhancedUser(0L, form.user, form.password, form.firstname, form.lastName, form.email, "")
    getEnhancedSchema(newEnhancedUser)
  }

  val testEU = EnhancedUser(0L, form.user, form.password, form.firstname, form.lastName, form.email, "")

  val testgetts = getEnhancedSchema(testEU)


  def getEnhancedSchema(enhancedUser: EnhancedUser): EnhancedSchema = {
    val salty = saltify(enhancedUser.password)
    val encryptingUser = encryptPW(enhancedUser.userName, salty, key)
    val saltPassword = convertPWStringToArrayBytes(salty)
    encryptEnhancedUser(enhancedUser, saltPassword,encryptingUser)
  }

  val saltpw = saltify("ruba9Grake")
  val encryptingbit = encryptPW("jsmith28", saltpw, key)
  //println("username: " + encryptingbit.userName + " " + encryptingbit.userName.length)
  //println("password: " + encryptingbit.password + " " + encryptingbit.password.length)
  //println("nonce: " + encryptingbit.passwordNonce + " " + encryptingbit.passwordNonce.length)


  def encryptPW(username: String, password: String, key: Array[Byte]): EncryptingUser = {
    val nonce = TestNonce.createNonce()
    val sourceText = password.getBytes(StandardCharsets.UTF_8)
    val cipher = box(key).encrypt(nonce.raw, sourceText)
    val nonceHex = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipher)
    val newEncryptingUser = EncryptingUser(username, nonceHex, cipherHex)
    newEncryptingUser
  }

  val salty = saltify("ruba9Grake")
  val saltPassword = convertPWStringToArrayBytes(salty)
  val testEU1: EnhancedUser = EnhancedUser(0L, form.user, form.password, form.firstname, form.lastName, form.email, "")
  val encryptingbit1 = encryptPW("jsmith28", saltpw, key)



  //val nonce = TestNonce.createNonce()
  /**val jsValue = Json.obj(
    "id" -> testEU1.id,
    "userName" -> testEU1.userName,
    "password" -> testEU1.password,
    "firstName" -> testEU1.firstName,
    "lastName" -> testEU1.lastName,
    "emailAddress" -> testEU1.emailAddress,
    "companyIds" -> testEU1.companyIds
  )
  **/
  //val stringData = Json.stringify(jsValue)
  //val sourceText = stringData.getBytes(StandardCharsets.UTF_8)
  //val cipher = box(key).encrypt(nonce.raw, sourceText)

  val eeu = encryptEnhancedUser(testEU1, saltPassword, encryptingbit1)

  //println("id " + eeu.id)
  //println("projectClass " + eeu.projectClass.length + " " + eeu.projectClass)
  //println("dataNonce " + eeu.dataNonce.length + " " +eeu.dataNonce)
  //println("dataValue " + eeu.dataValue.length + " " +eeu.dataValue)
  //println("userName " + eeu.userName.length + " " +eeu.userName)
  //println("passwordNonce " + eeu.passwordNonce.length + " " +eeu.passwordNonce)
  //println("passwordEncrypted " + eeu.passwordEncrypted.length + " " +eeu.passwordEncrypted)

  def encryptEnhancedUser(enhancedUser: EnhancedUser, key: Array[Byte], encryptingUser: EncryptingUser): EnhancedSchema = {
    val nonce = TestNonce.createNonce()
    //val jsValue = Json.toJson(enhancedUser)
    val jsValue = Json.obj(
      "id" -> enhancedUser.id,
      "userName" -> enhancedUser.userName,
      "password" -> enhancedUser.password,
      "firstName" -> enhancedUser.firstName,
      "lastName" -> enhancedUser.lastName,
      "emailAddress" -> enhancedUser.emailAddress,
      "companyIds" -> enhancedUser.companyIds
    )
    val stringData = Json.stringify(jsValue)
    val sourceText = stringData.getBytes(StandardCharsets.UTF_8)
    val cipher = box(key).encrypt(nonce.raw, sourceText)
    val nonceHex = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipher)

    val newEnhancedSchema = EnhancedSchema(0L,
      "EnhancedUser",
      nonceHex,
      cipherHex,
      encryptingUser.userName,
      encryptingUser.passwordNonce,
      encryptingUser.password,
    "")

    newEnhancedSchema
  }

  ///////////////////////////////////////////
  //////////////////////////////////////////
  /////////////////////////////////////////
  private def box(secretKey: Array[Byte]) = {
    new org.abstractj.kalium.crypto.SecretBox(secretKey)
  }
  private def encoder = org.abstractj.kalium.encoders.Encoder.HEX

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

  ////////////////
  ///////////////

  //def login(username: String, password: String): EnhancedUser = {
  //  val candidateSchema = eeu
  //  val enhancedUser = testcheckPasswordReturnResult(password, candidateSchema)
  //}

  //val testcheckpw = testcheckPasswordReturnResult("ruba9Grake", eeu)

  def testdecryptPassword(encryptingUser: EncryptingUser, thiskey: Array[Byte]): Array[Byte] = {
    val nonceHex = encryptingUser.passwordNonce
    val cipherHex = encryptingUser.password
    val decryptedNonce = TestNonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)

    val decryptedRaw = box(thiskey).decrypt(decryptedNonce.raw, decryptedCipher)

    decryptedRaw
  }

  val testpassword = "abcdefghijklmnopqrstuvwxyz123456"

  val passwordobject = encryptPW("jsmith28", testpassword, key)

  val testnonceHex = passwordobject.passwordNonce
  val testcipherHex = passwordobject.password
  val testdecryptedNonce = TestNonce.nonceFromBytes(encoder.decode(testnonceHex))
  val testdecryptedCipher = encoder.decode(testcipherHex)

  val testdecryptedRaw = box(key).decrypt(testdecryptedNonce.raw, testdecryptedCipher)
  val testsstringData = new String(testdecryptedRaw, StandardCharsets.UTF_8)
  val testmakearrayagain = convertPWStringToArrayBytes(testsstringData)
  val makethismessastringagain = new String(testmakearrayagain, StandardCharsets.UTF_8)
  println("////////////////////////////")
  //println("key as array length: " + testsecretKey.length)
  //println(testsecret + " length: " + testsecret.length)
  println("testpassword: " + testpassword)
  println("test password length: " + testpassword.length)
  println("test pw array bytes length: " + testdecryptedRaw.length)
  println("teststringdecoded: " + testsstringData)
  println("length of array after making array again: " + testmakearrayagain.length)
  println("made string again: " + makethismessastringagain)
  //println(makethismessastringagain)
  println("////////////////////////////")

  val newEncryptingUser = EncryptingUser(eeu.userName,
    eeu.passwordNonce,
    eeu.passwordEncrypted)

  val candidatePasswordArray = testdecryptPassword(newEncryptingUser, key)
  println(candidatePasswordArray.length)
  val candidatePasswordArrayAsString = new String(candidatePasswordArray, StandardCharsets.UTF_8)
  println(candidatePasswordArrayAsString + " length: " + candidatePasswordArrayAsString.length)
  val turnitbackintoanarray = convertPWStringToArrayBytes(candidatePasswordArrayAsString)
  val backtoastringfinally = new String(turnitbackintoanarray, StandardCharsets.UTF_8)

  println("/////////////////////////////")
  println("try to fix it")
  println("////////////////////////////")
  //val test2 = "r<ﾃ3zﾈﾻJBNￒFV>ﾳﾽjﾺ4ﾤDxX￦2ﾯ"
  println("start array length: " + candidatePasswordArray.length)
  println("start array turned into string: " + candidatePasswordArrayAsString + " length: " + candidatePasswordArrayAsString.length)
  println("turn the string back into an array length: " + turnitbackintoanarray.length)
  println("finally back to a string: " + backtoastringfinally)
  println(backtoastringfinally == candidatePasswordArrayAsString)
  //val tdeu = testdecryptEnhancedUser(eeu, candidatePasswordArray)

  println("///////////////////////////")
  println("keep testing")
  println("///////////////////////////")
  val encryptingUsertest2 = encryptPW("jdsmith28", "abcdefghijklmnopqrstuvwxyz123456", key)
  val test2 = testdecryptPassword(encryptingUsertest2, key)
  println("test 2 array length: " + test2.length)

  val nonceHex = eeu.dataNonce
  val cipherHex = eeu.dataValue
  val decryptedNonce = TestNonce.nonceFromBytes(encoder.decode(nonceHex))
  val decryptedCipher = encoder.decode(cipherHex)

  //val decryptedRaw = box(candidatePasswordArray).decrypt(decryptedNonce.raw, decryptedCipher)

  def testdecryptEnhancedUser(enhancedSchema: EnhancedSchema, thiskey: Array[Byte]): Option[EnhancedUser] = {
    val nonceHex = enhancedSchema.dataNonce
    val cipherHex = enhancedSchema.dataValue
    val decryptedNonce = TestNonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)
    val decryptedRaw = box(thiskey).decrypt(decryptedNonce.raw, decryptedCipher)
    val stringData = new String(decryptedRaw, StandardCharsets.UTF_8)

    val jsonData = Json.parse(stringData)
    val enhancedUser = getEnhancedUserFromJson(jsonData).get

    Some(EnhancedUser(enhancedSchema.id,
      enhancedUser.userName,
      enhancedUser.password,
      enhancedUser.firstName,
      enhancedUser.lastName,
      enhancedUser.emailAddress,
      enhancedUser.companyIds))

  }

  def testcheckPasswordReturnResult(inputPassword: String, candidateSchema: EnhancedSchema): EnhancedUser = {
    val newEncryptingUser = EncryptingUser(candidateSchema.userName,
      candidateSchema.passwordNonce,
      candidateSchema.passwordEncrypted)

    val candidatePasswordArray = Encrypt().decryptPassword(newEncryptingUser, key)
    val candidateFullPassword = new String(candidatePasswordArray, StandardCharsets.UTF_8)
    val candidatePassword = candidateFullPassword.substring(0,inputPassword.length)

    val checkResult = if(inputPassword == candidatePassword) {
      (Encrypt().decryptEnhancedUser(candidateSchema, candidatePasswordArray)).get
    }else{
      EnhancedUser(0L, "", "", "", "", "", "")
    }
    checkResult
  }


}

object testseq extends App {

  val seq = Seq("abc ltd.", "xyz corp")
  val string = "a"
  val newList = seq.filter(_.toLowerCase.contains(string.toLowerCase))

  println(newList)

}




