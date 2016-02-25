package uk.mm.mpp.actors

import akka.actor.{Actor, PoisonPill, Props}
import org.json4s.{JArray, JValue}
import org.slf4j.LoggerFactory
import uk.mm.mpp.actors.ResultActor.{AllRecordsReceived, FullResult, PartialUpdate, UpdateRequest}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object ResultActor {
  def props(uid: String) = Props(classOf[ResultActor], uid)

  case class FullResult(products: JArray)

  case class PartialUpdate(data: JValue)

  case class AllRecordsReceived()

  case class UpdateRequest()

}

class ResultActor(uid: String) extends Actor {
  protected implicit def executor: ExecutionContext = context.dispatcher

  val logger = LoggerFactory.getLogger(getClass.getSimpleName + uid)
  val products = ListBuffer.empty[JValue]
  var updatesActive = true
  var allRecordsReceived = false

  def receive = {
    case PartialUpdate(data) =>
      products ++= data.asInstanceOf[JArray].arr

    case UpdateRequest =>
      if (updatesActive) answerWithProducts()
      else answerWithAllRecordsReceived()

      if (allRecordsReceived) {
        updatesActive = false
        products.clear()
      }

    case AllRecordsReceived =>
      logger.info("no more data reception!")
      allRecordsReceived = true
      context.system.scheduler.scheduleOnce(3 seconds) {
        self ! PoisonPill
      }
  }

  def answerWithProducts() = {
    val copy = JArray(List() ++ products)
    sender ! FullResult(copy)
  }

  def answerWithAllRecordsReceived() = {
    sender ! AllRecordsReceived
  }

  override def postStop {
    logger.info("shut down")
  }
}
