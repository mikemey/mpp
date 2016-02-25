package uk.mm.mpp.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import org.apache.commons.lang3.StringUtils.{abbreviate, replacePattern}
import org.json4s._
import org.json4s.native.JsonMethods._
import play.api.Logger

object Providers {
  val ports = List(2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024)
}

object ProductActor {
  def props(uid: String, resultActor: ActorRef) = Props(classOf[ProductActor], uid, resultActor)

  case class ProductRequest()

  case class ProductResponse(sourceUrl: String, products: JValue)

  case class ErrorResponse(status: String, message: JValue)

}

class ProductActor(uid: String, resultActor: ActorRef) extends Actor {
  val logger = Logger(getClass.getSimpleName + uid)
  val providerActors =
    Providers.ports.map(port => context.actorOf(ProviderActor.props(port), "Provider_" + port))

  var remainingProviders = providerActors.size

  def shutdownProviderActor(providerActor: ActorRef) = {
    providerActor ! PoisonPill
    remainingProviders -= 1
    if (remainingProviders == 0) {
      self ! PoisonPill
      resultActor ! AllRecordsReceived
    }
  }

  def receive = {
    case ProductRequest =>
      logger.info("received product request.")
      providerActors.foreach(provider => provider ! ProductRequest)

    case ProductResponse(source, products) =>
      logger.info(s"from: [$source] received: [${piedpiper(products)}]")
      resultActor ! PartialUpdate(products)
      shutdownProviderActor(sender)

    case ErrorResponse(status, msg) =>
      logger.error(s"received error code [$status]: $msg")
      shutdownProviderActor(sender)
  }

  def piedpiper(json: JValue) = {
    val message = compact(render(json))
    abbreviate(replacePattern(message, """\s{2,}""", " "), 30)
  }

  override def postStop {
    logger.info("shut down")
  }
}