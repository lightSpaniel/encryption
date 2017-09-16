package services

import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
import org.abstractj.kalium.crypto.Random
import java.nio.charset.StandardCharsets

import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Some of this is taken and adapted from the Play Kalium encryption
  * tutorial found at. The Nonce object was not changed from this example.
  * https://github.com/playframework/play-scala-secure-session-example
  *
  *
  */

trait Encryption


class Nonce(val raw: Array[Byte]) extends AnyVal

object Nonce{
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


case class Encrypt() extends Encryption {

  private def box(secretKey: Array[Byte]) = {
    new org.abstractj.kalium.crypto.SecretBox(secretKey)
  }

  private def encoder = org.abstractj.kalium.encoders.Encoder.HEX

  def getSessionKey(): String = {
    val nonce = Nonce.createNonce()
    val nonceHex = encoder.encode(nonce.raw)
    nonceHex
  }

//Encrypt
  def encryptEnhancedUser(enhancedUser: EnhancedUser, key: Array[Byte], encryptingUser: EncryptingUser): EnhancedSchema = {
    val nonce = Nonce.createNonce()
    val jsValue = Json.toJson(enhancedUser)
    val stringData = Json.stringify(jsValue)
    val sourceText = stringData.getBytes(StandardCharsets.UTF_8)
    val cipher = box(key).encrypt(nonce.raw, sourceText)

    val nonceHex = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipher)

    val sessionKey = getSessionKey

    val newEnhancedSchema = EnhancedSchema(0L,
      "EnhancedUser",
      nonceHex,
      cipherHex,
      encryptingUser.userName,
      encryptingUser.passwordNonce,
      encryptingUser.password,
      sessionKey)

    newEnhancedSchema

  }

  def encryptPassword(username: String, password: String, key: Array[Byte]): EncryptingUser = {
    val nonce = Nonce.createNonce()
    val sourceText = password.getBytes(StandardCharsets.UTF_8)
    val cipher = box(key).encrypt(nonce.raw, sourceText)

    val nonceHex = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipher)

    val newEncryptingUser = EncryptingUser(username, nonceHex, cipherHex)

    newEncryptingUser
  }


  def encrypt (jsValue: JsValue, key: Array[Byte], projectClass: String): Schema = {
    val nonce = Nonce.createNonce()

    val stringData = Json.stringify(jsValue)
    val sourceText = stringData.getBytes(StandardCharsets.UTF_8)
    val cipher = box(key).encrypt(nonce.raw, sourceText)

    val nonceHex = encoder.encode(nonce.raw)
    val cipherHex = encoder.encode(cipher)

    val newSchema = Schema(0, projectClass, nonceHex, cipherHex)

    newSchema
  }



//Decrypt
  def decryptPassword(encryptingUser: EncryptingUser, key: Array[Byte]): Array[Byte] = {
    val nonceHex = encryptingUser.passwordNonce
    val cipherHex = encryptingUser.password
    val decryptedNonce = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)

    val decryptedRaw = box(key).decrypt(decryptedNonce.raw, decryptedCipher)

    decryptedRaw
  }

  def decryptEnhancedUser(enhancedSchema: EnhancedSchema, key: Array[Byte]): Option[EnhancedUser] = {
    val nonceHex = enhancedSchema.dataNonce
    val cipherHex = enhancedSchema.dataValue
    val decryptedNonce = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)
    val decryptedRaw = box(key).decrypt(decryptedNonce.raw, decryptedCipher)
    val stringData = new String(decryptedRaw, StandardCharsets.UTF_8)

    val jsonData = Json.parse(stringData)
    val enhancedUser = getEnhancedUserFromJson(jsonData).get

    Some(EnhancedUser(enhancedSchema.id,
      enhancedUser.userName,
      enhancedUser.password,
      enhancedUser.firstName,
      enhancedUser.lastName,
      enhancedUser.emailAddress))
  }



  private def decryptGeneral(schema: Schema, key: Array[Byte]): JsValue = {
    val nonceHex = schema.dataNonce
    val cipherHex = schema.dataValue
    val decryptedNonce = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)
    val decryptedRaw = box(key).decrypt(decryptedNonce.raw, decryptedCipher)
    val stringData = new String(decryptedRaw, StandardCharsets.UTF_8)

    Json.parse(stringData)
  }


  def decryptBid (schema: Schema, key: Array[Byte]): Option[Bid] = {
    val json = decryptGeneral(schema, key)
    val bid = getBidFromJson(json).get
    Some(Bid(schema.id, bid.currency, bid.amount, bid.bidType, bid.ventureId, bid.userId, bid.nominal))
  }

  def decryptVenture (schema: Schema, key: Array[Byte]): Option[Venture] = {
    val json = decryptGeneral(schema, key)
    val venture = getVentureFromJson(json).get
    Some(Venture(schema.id,
      venture.name,
      venture.sectorId,
      venture.profit,
      venture.turnover,
      venture.price,
      venture.numberOfShares))
  }





}



