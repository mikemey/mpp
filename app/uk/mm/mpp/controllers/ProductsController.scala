package uk.mm.mpp.controllers

import javax.inject.{Inject, _}

import akka.actor.{ActorSystem, _}
import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.json4s.native.JsonMethods
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import uk.mm.mpp.actors.ResultActor.{AllRecordsReceived, FullResult, UpdateRequest, resultActorName}
import uk.mm.mpp.actors.SearchActor.ProductRequest
import uk.mm.mpp.actors.{DataSocketActor, ResultActor, SearchActor}
import uk.mm.mpp.globals.MPP_PREFIX

import scala.concurrent.Await.result
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class ProductsController @Inject()(system: ActorSystem) extends Controller {
  implicit val timeout = new Timeout(2 seconds)

  val logger = Logger(MPP_PREFIX + getClass.getSimpleName)
  val returnInvalidActorMessage = Gone("""{ "error": "Invalid actor id" }"""")

  def products = Action { implicit request =>
    val uid = randomAlphabetic(5)

    logger.info(s"new search request received, creating workers for $uid...")
    val resultActor = system.actorOf(ResultActor.props(uid), resultActorName(uid))
    val searchActor = system.actorOf(SearchActor.props(uid, resultActor))
    searchActor ! ProductRequest

    val socketLocation = routes.ProductsController.socket(uid).webSocketURL()
    logger.debug(s"redirecting to socket url: [$socketLocation]")
    Accepted(s"""{ "location": "$socketLocation" }""")
  }

  def socket(uid: String) = WebSocket.acceptWithActor[String, String] { request => out =>
    DataSocketActor.props(out, uid)
  }

  private def toHttpResult(update: Any): Result = update match {
    case FullResult(data) => Ok(JsonMethods.compact(JsonMethods.render(data)))
    case AllRecordsReceived => ResetContent
    case _ => Gone("""{ "error": "No response from result actor." }"""")
  }
}