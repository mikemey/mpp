package uk.mm.mpp.controllers

import javax.inject.{Inject, _}

import uk.mm.mpp.actors.{ResultActor, ProductActor}
import ProductActor.ProductRequest
import ResultActor.{AllRecordsReceived, FullResult, UpdateRequest}
import actors.ResultActor
import akka.actor.{ActorSystem, _}
import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.json4s.native.JsonMethods
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Await.result
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class Application @Inject()(system: ActorSystem) extends Controller {
  implicit val timeout = new Timeout(2 seconds)

  val logger = Logger("MPP-Server")
  val returnInvalidActorMessage = Gone("""{ "error": "Invalid actor id" }"""")

  def resultActorName(uid: String) = "ResultActor_" + uid

  def products = Action {
    logger.info("received get request")

    val uid: String = "_" + randomAlphabetic(5)
    logger.info(s"creating workers for $uid")
    val resultActor = system.actorOf(ResultActor.props(uid), resultActorName(uid))
    val productActor = system.actorOf(ProductActor.props(uid, resultActor))

    productActor ! ProductRequest
    Accepted(s"""{ "location": "/api/results/$uid" }""")
  }

  def results(uid: String) = Action.async {
    searchForResultActor(uid)
      .map(resultActor => (resultActor ? UpdateRequest).map(toHttpResult))
      .getOrElse(Future.successful(returnInvalidActorMessage))
  }

  def searchForResultActor(uid: String) = {
    val raName = "user/" + resultActorName(uid)
    val raSelection = system.actorSelection(raName)
    try {
      Some(result(raSelection.resolveOne, 1 seconds))
    } catch {
      case anf: ActorNotFound => None
    }
  }

  def toHttpResult(update: Any): Result = update match {
    case FullResult(data) => Ok(JsonMethods.compact(JsonMethods.render(data)))
    case AllRecordsReceived => ResetContent
    case _ => Gone("""{ "error": "No response from result actor." }"""")
  }
}