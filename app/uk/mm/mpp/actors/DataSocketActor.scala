package uk.mm.mpp.actors

import akka.actor._
import akka.util.Timeout
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._
import play.api.Logger
import uk.mm.mpp.actors.DataSocketActor.{AllRecordsReceived, PartialUpdate, STOP_MESSAGE}
import uk.mm.mpp.actors.SearchActor.ProductRequest
import uk.mm.mpp.globals._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object DataSocketActor {
  def props(out: ActorRef) = Props(classOf[DataSocketActor], out)

  val STOP_MESSAGE = """{ "message": "done" }"""

  case class PartialUpdate(data: List[JValue])

  case class AllRecordsReceived()

}

class DataSocketActor(out: ActorRef) extends Actor {
  protected implicit def executor: ExecutionContext = context.dispatcher

  implicit val timeout = new Timeout(2 seconds)
  val logger = Logger(MPP_PREFIX + getClass.getSimpleName)

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
      out ! STOP_MESSAGE
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