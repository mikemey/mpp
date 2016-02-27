package uk.mm.mpp.actors

import akka.actor._
import play.api.Logger
import uk.mm.mpp.actors.ResultActor._
import uk.mm.mpp.globals._

import scala.concurrent.Await._
import scala.concurrent.duration._

object DataSocketActor {
  def props(out: ActorRef, uid: String) = Props(classOf[DataSocketActor], out, uid)
}

class DataSocketActor(out: ActorRef, uid: String) extends Actor {
  val logger = Logger(MPP_PREFIX + getClass.getSimpleName + "_" + uid)

  def receive = {
    case msg: String =>
      logger.warn(s"received [$msg]")
      out ! "[{ name: cname, offerUrl: 'http://www.google.com', desc: 'Lorem Ipsum', imgUrl: 'http://www.credit-card-logos.com/images/multiple_credit-card-logos-2/credit_card_logos_29.gif' }]"
  }

  override def postStop() = {
    logger.info("stopped.")
  }

  private def searchForResultActor(uid: String) = {
    val raSelection = context.actorSelection(resultActorLookupName(uid))
    try {
      Some(result(raSelection.resolveOne, 1 seconds))
    } catch {
      case anf: ActorNotFound => None
    }
  }

  private def resultActorLookupName(uid: String) = "user/" + resultActorName(uid)
}