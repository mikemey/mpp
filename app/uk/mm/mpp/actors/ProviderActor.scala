package uk.mm.mpp.actors

import akka.actor.{Actor, Props}
import org.apache.commons.lang3.StringUtils._
import org.json4s._
import org.json4s.native.JsonMethods._
import play.api.Logger
import uk.mm.mpp.actors.ProviderActor.{ProductRequest, ProductResponse}
import uk.mm.mpp.globals._

import scala.concurrent.ExecutionContext
import scalaj.http.{BaseHttp, HttpOptions, HttpResponse}

object ProviderHttp extends BaseHttp(options = Seq(
  HttpOptions.connTimeout(2000),
  HttpOptions.readTimeout(15000),
  HttpOptions.followRedirects(false)
))

object ProviderActor {
  def props(uid: String, port: Int) = Props(classOf[ProviderActor], uid, port)

  case class ProductRequest()

  case class ProductResponse(products: JArray)

}

class ProviderActor(uid: String, port: Int) extends Actor {
  protected implicit def executor: ExecutionContext = context.dispatcher

  val logger = Logger(MPP_WORKER_PREFIX + getClass.getSimpleName + "_" + uid + "_" + port)
  val providerUrl: String = "http://localhost:" + port + "/3rd/products"

  def receive = {
    case ProductRequest =>
      val response: HttpResponse[String] = ProviderHttp(providerUrl).asString
      sender ! productUpdateFrom(response)
  }

  def productUpdateFrom(response: HttpResponse[String]) = if (response.isSuccess) {
    logger.debug(s"from: [$providerUrl]: [${piedPiper(response)}]")
    ProductResponse(parseJsonFrom(response))
  } else {
    logger.warn(s"from: [$providerUrl]: [${response.body}]")
    ProductResponse(JArray(List()))
  }

  def piedPiper(response: HttpResponse[String]) = {
    abbreviate(replacePattern(response.body, """\s{2,}""", " "), 30)
  }

  def parseJsonFrom(response: HttpResponse[String]) = parse(response.body).asInstanceOf[JArray]
}
