package uk.mm.mpp.actors

import akka.actor._


object DataSocketActor {
  def props(out: ActorRef, uid: String) = Props(classOf[DataSocketActor], out, uid)
}

class DataSocketActor(out: ActorRef, uid: String) extends Actor {
  def receive = {
    case msg: String =>
      out ! "[{ name: cname, offerUrl: 'http://www.google.com', desc: 'Lorem Ipsum', imgUrl: 'http://www.credit-card-logos.com/images/multiple_credit-card-logos-2/credit_card_logos_29.gif' }]"
  }
}

//val raName = "user/" + resultActorName(uid)
//val raSelection = system.actorSelection(raName)
//try {
//  Some(result(raSelection.resolveOne, 1 seconds))
//} catch {
//  case anf: ActorNotFound => None
//}