
import models._

import org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES
import org.abstractj.kalium.crypto.Random
import java.nio.charset.StandardCharsets

import models._
import play.api.libs.json._

import play.api.libs.functional.syntax._

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

object testdecrypt extends App {

  val schema = Schema(1, "VENTURE",
    "2badab206f24ae5bd9e716f135f3d578126890a9e277fc24",
"f3ee03854700c7204baae070b29af539aa25eba2b1956686bc334e83db2446b179d436e29b5546ac50b144af9fc477e49778747fff565814e4c925c9db0a0a8fc47ea818ebc1a8d318bf00cc7e4f6f00aa87d4b667fd5465c00de879f43ab0e7c45628c0f57aaa60624fdf0c8921612ea1da6795324903d25c"
  )

  private def box(secretKey: Array[Byte]) = {
    new org.abstractj.kalium.crypto.SecretBox(secretKey)
  }

  private def encoder = org.abstractj.kalium.encoders.Encoder.HEX

  val secret = "8tinAB6qnWnKMnbgT6khJLcxDdbBSjfR"
  val key = (for(s <- secret) yield {s.toByte}).toArray

  private def decryptGeneral(schema: Schema, key: Array[Byte]): JsValue = {
    val nonceHex = schema.dataNonce
    val cipherHex = schema.dataValue
    val decryptedNonce = Nonce.nonceFromBytes(encoder.decode(nonceHex))
    val decryptedCipher = encoder.decode(cipherHex)
    val decryptedRaw = box(key).decrypt(decryptedNonce.raw, decryptedCipher)
    val stringData = new String(decryptedRaw, StandardCharsets.UTF_8)

    Json.parse(stringData)
  }
    val json = decryptGeneral(schema, key)

  val testJson = Json.obj(
    "id" -> 1,
    "name" -> "Cats",
    "sector" -> 3,
    "profit" -> 10.0,
    "turnover" -> 20.0,
    "price" -> 12.6,
    "numberOfShares" -> 34
  )

  val newtestJson = Json.obj(
    "id" -> 1,
    "name" -> "hello",
    "sector" -> 4,
    "profit" -> 10.0,
    "turnover" -> 20.0,
    "price" -> 12.6,
    "numberOfShares" -> 34
  )


  case class testthing(id: Long, name: String, sector: Long, profit: Double, turnover: Double, price: Double, numberOfShares: Long)

  implicit val readsTestThing: Reads[testthing] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "sector").read[Long]and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]

  )(testthing.apply _)

  //println(res)

  def getVentureFromJsonTest(json: JsValue): Option[Venture] = {
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


  implicit val readsVenture: Reads[Venture] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "sector").read[Long] and
      (JsPath \ "profit").read[Double] and
      (JsPath \ "turnover").read[Double] and
      (JsPath \ "price").read[Double] and
      (JsPath \ "numberOfShares").read[Long]
    )(Venture.apply _)

  println(json)
  println(testJson)
  println(newtestJson)

  val t = newtestJson.validate[testthing]
  val s = testJson.validate[Venture]
  val x = json.validate[Venture]

  println(t)
  println(s)
  println(x)

}

object testSe extends App {

  case class testItem(id: Long, name: String)

  def seqtest: Seq[testItem] = {

    val one = testItem(1, "cats")
    val two = testItem(2, "dogs")
    val three = testItem(3, "bears")

    Seq(one, two, three)

  }


  def testfilter(toFilter: String): Boolean = {
    seqtest.filter(_.name == toFilter).length match {
      case 0 => true
      case _ => false
    }
  }

  val test1 = testfilter("hello")
  println(test1)

  val test2 = testfilter("cats")
  println(test2)

}