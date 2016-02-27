package uk.mm.mpp.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import org.json4s._
import play.api.Logger
import uk.mm.mpp.actors.DataSocketActor.{AllRecordsReceived, PartialUpdate}
import uk.mm.mpp.actors.SearchActor.{ErrorResponse, PORTS, ProductRequest, ProductResponse}
import uk.mm.mpp.globals.MPP_WORKER_PREFIX

object SearchActor {
  val PORTS = List(2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024)

  def props(uid: String, dataCollectorActor: ActorRef) = Props(classOf[SearchActor], uid, dataCollectorActor)

  case class ProductRequest()

  case class ProductResponse(products: List[JValue])

  case class ErrorResponse(status: String, message: JValue)

}

class SearchActor(uid: String, dataCollectorActor: ActorRef) extends Actor {
  val logger = Logger(MPP_WORKER_PREFIX + getClass.getSimpleName + "_" + uid)
  val providerActors = PORTS.map(port => context.actorOf(ProviderActor.props(uid, port)))

  var remainingProviders = providerActors.size

  override def preStart() = {
    logger.debug(s"started as [${self.path.name}]")
  }

  def receive = {
    case ProductRequest =>
      logger.info("starting product search...")
      providerActors.foreach(provider => provider ! ProductRequest)

    case ProductResponse(products) =>
      dataCollectorActor ! PartialUpdate(products)
      shutdownProviderActor(sender)

    case ErrorResponse(status, msg) => shutdownProviderActor(sender)
  }

  def shutdownProviderActor(providerActor: ActorRef) = {
    providerActor ! PoisonPill
    remainingProviders -= 1
    if (remainingProviders == 0) {
      dataCollectorActor ! AllRecordsReceived
      self ! PoisonPill
    }
  }

  override def postStop {
    logger.info("stopped.")
  }
}