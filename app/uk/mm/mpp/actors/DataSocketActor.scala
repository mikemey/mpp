package uk.mm.mpp.actors

import akka.actor._
import akka.util.Timeout
import org.json4s._
import org.json4s.native.JsonMethods._
import play.api.Logger
import uk.mm.mpp.actors.ProviderActor.ProductRequest
import uk.mm.mpp.actors.SearchActor.{AllRecordsReceived, PartialUpdate}
import uk.mm.mpp.globals._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object DataSocketActor {
  def props(out: ActorRef) = Props(classOf[DataSocketActor], out)
}

class DataSocketActor(out: ActorRef) extends Actor {
  protected implicit def executor: ExecutionContext = context.dispatcher

  implicit val timeout = new Timeout(2 seconds)
  val logger = Logger(MPP_WORKER_PREFIX + getClass.getSimpleName)

  override def preStart() = {
    logger.debug(s"started.")
  }

  def receive = {
    case message: String =>
      logger.debug(s"received message [$message]. starting up workers...")
      val uid = (parse(message) \\ "query").asInstanceOf[JString].values
      startSearch(uid)
    case PartialUpdate(data) =>
      logger.debug("received partial update.")
      out ! compact(render(data))
    case AllRecordsReceived =>
      logger.debug("received all records received.")
      self ! PoisonPill
    case msg => logger.error(s"UNKNOWN: [${msg.getClass}] [$msg]")
  }

  def startSearch(uid: String) = {
    logger.info(s"query received: [$uid]")
    val searchActor = context.actorOf(SearchActor.props(uid, self))
    searchActor ! ProductRequest
  }

  override def postStop() = {
    logger.info("stopped.")
  }
}