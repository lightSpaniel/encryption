package actors

import akka.actor._
import javax.inject._

import com.google.inject.assistedinject.Assisted
import models.{BidAccess, VentureAccess}
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import services.Encryption

import scala.concurrent.Await
import scala.concurrent.duration._

object ParentActor {
  case class GetChild(key: String)
  case class ExecuteBid(bidNominal: Long, ventureId: Long)
}

class ParentActor @Inject() (childFactory: ConfiguredChildActor.Factory,
                             encryption: Encryption,
                             ventureAccess: VentureAccess,
                             bidAccess: BidAccess,
                             configuration: Configuration,
                             @Assisted key: String
                            ) extends Actor with InjectedActorSupport {
  import ParentActor._

  def receive = {
    case GetChild(key: String) =>
      val child: ActorRef = injectedChild(childFactory(key), key)
      sender() ! child
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
  }
}
