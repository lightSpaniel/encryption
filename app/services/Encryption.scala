package services

import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
import org.abstractj.kalium.crypto.Random
import java.nio.charset.StandardCharsets

import com.google.inject.ImplementedBy
import controllers.HomeController
import models._
import play.api.libs.json._

import javax.inject._

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

  private def decryptGeneral(schema: Schema, key: Array[Byte]): JsValue = {
    val nonceHex = schema.dataNonce
    val cipherHex = schema.dataValue
    val decryptedNonce = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)
    val decryptedRaw = box(key).decrypt(decryptedNonce.raw, decryptedCipher)
    val stringData = new String(decryptedRaw, StandardCharsets.UTF_8)

    Json.parse(stringData)
  }

  def decryptUser (schema: Schema, key: Array[Byte]): Option[User] = {
    val json = decryptGeneral(schema, key)
    getUserFromJson(json)
  }

  def decryptBid (schema: Schema, key: Array[Byte]): Option[Bid] = {
    val json = decryptGeneral(schema, key)
    getBidFromJson(json)
  }

  def decryptVenture (schema: Schema, key: Array[Byte]): Option[Venture] = {
    val json = decryptGeneral(schema, key)
    getVentureFromJson(json)
  }


}



