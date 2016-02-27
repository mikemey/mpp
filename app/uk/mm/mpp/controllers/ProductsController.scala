package uk.mm.mpp.controllers

import javax.inject.{Inject, _}

import akka.actor.ActorSystem
import akka.util.Timeout
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import play.api.Play.current
import play.api._
import play.api.mvc._
import uk.mm.mpp.actors.DataSocketActor
import uk.mm.mpp.globals.MPP_PREFIX

import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class ProductsController @Inject()(system: ActorSystem) extends Controller {
  implicit val timeout = new Timeout(2 seconds)

  val logger = Logger(MPP_PREFIX + getClass.getSimpleName)
  val returnInvalidActorMessage = Gone("""{ "error": "Invalid actor id" }"""")

  def products = Action { implicit request =>
    val uid = randomAlphabetic(5)

    val socketLocation = routes.ProductsController.socket().webSocketURL()
    logger.debug(s"redirecting to socket url: [$socketLocation]")
    Accepted(
      s"""{ "location": "$socketLocation",
          |  "uid": "$uid" }""".stripMargin)
  }

  def socket() = WebSocket.acceptWithActor[String, String] { request => out =>
    DataSocketActor.props(out)
  }
}