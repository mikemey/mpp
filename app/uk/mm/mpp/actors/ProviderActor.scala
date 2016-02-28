package uk.mm.mpp.actors

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import org.apache.commons.lang3.StringUtils._
import org.json4s._
import org.json4s.native.JsonMethods._
import play.api.Logger
import play.api.Play.current
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import uk.mm.mpp.actors.ProviderActor.{ProductRequest, ProductResponse}
import uk.mm.mpp.globals._

import scala.concurrent.ExecutionContext.Implicits.global

object ProviderActor {
  def props(uid: String, port: Int) = Props(classOf[ProviderActor], uid, port)

  case class ProductRequest()

  case class ProductResponse(products: JArray)

}

class ProviderActor(uid: String, port: Int) extends Actor {

  private lazy val request: WSRequest = WS.client.url(providerUrl)
    .withFollowRedirects(false)
    .withRequestTimeout(15000)

  val logger = Logger(MPP_WORKER_PREFIX + getClass.getSimpleName + "_" + uid + "_" + port)
  val providerUrl: String = "http://localhost:" + port + "/3rd/products"

  def receive = {
    case ProductRequest =>
      request.get()
        .map(productUpdateFrom)
        .recover(withEmptyJsonArray)
        .pipeTo(sender)
  }

  val withEmptyJsonArray: PartialFunction[Throwable, ProductResponse] = {
    case _ => ProductResponse(JArray(List()))
  }

  def productUpdateFrom(response: WSResponse): ProductResponse = if (response.status == 200) {
    logger.debug(s"from: [$providerUrl]: [${piedPiper(response)}]")
    ProductResponse(parseJsonFrom(response))
  } else {
    logger.warn(s"from: [$providerUrl]: [${response.body}]")
    ProductResponse(JArray(List()))
  }

  def piedPiper(response: WSResponse) = {
    abbreviate(replacePattern(response.body, """\s{2,}""", " "), 30)
  }

  def parseJsonFrom(response: WSResponse) = parse(response.body).asInstanceOf[JArray]
}
