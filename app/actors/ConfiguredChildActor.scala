package actors

import akka.actor._
import javax.inject._

import com.google.inject.assistedinject.Assisted
import play.api.Configuration
import models.{BidAccess, TestCC, VentureAccess}
import services.{Encrypt, Encryption}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object ConfiguredChildActor {
  case object GetConfig
  case class ExecuteBid(bidNominal: Long, ventureId: Long)

  trait Factory {
    def apply(key: String): Actor
  }
}

class ConfiguredChildActor @Inject() (encryption: Encryption,
                                      ventureAccess: VentureAccess,
                                      bidAccess: BidAccess,
                                      configuration: Configuration,
                                      @Assisted key: String) extends Actor {
  import ConfiguredChildActor._

  val config = configuration.getString(key).getOrElse("none")

  def receive = {
    case GetConfig =>
      sender() ! config
    case ExecuteBid(bidNominal: Long, ventureId: Long) => {
      val futureVenture = ventureAccess.get(ventureId)
      val venture = (Await.result(futureVenture, 5.seconds)).get
      val ventureNominal = venture.numberOfShares

      val result = if (bidNominal < ventureNominal){
        val f = ventureAccess.updateNumberOfShares(ventureNominal-bidNominal, ventureId)
        Await.result(f, 5.seconds)
      }else{
        "fail"
      }

      result match{
        case "numberOfShares updated" => {
          sender() ! "Sucess"
        }
        case _ => "Failure"
      }
    }
    case TestCC(bidNominal: Long, ventureId: Long, testString: String, key: Array[Byte]) => {
      val futureVentureSchema = ventureAccess.get(ventureId)
      val ventureSchema = (Await.result(futureVentureSchema, 5.seconds)).get
      val venture = (Encrypt().decryptVentureNew(ventureSchema, key)).get

      if(bidNominal <= venture.numberOfShares) {

      }

    }
  }
}
