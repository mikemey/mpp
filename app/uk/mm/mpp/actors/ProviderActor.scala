package uk.mm.mpp.actors

import akka.actor.{Actor, Props}
import org.json4s._
import org.json4s.native.JsonMethods._
import uk.mm.mpp.actors.ProductActor.{ErrorResponse, ProductRequest, ProductResponse}

import scala.concurrent.ExecutionContext
import scalaj.http.{BaseHttp, HttpOptions, HttpResponse}

object ProviderHttp extends BaseHttp(options = Seq(
  HttpOptions.connTimeout(2000),
  HttpOptions.readTimeout(15000),
  HttpOptions.followRedirects(false)
))

object ProviderActor {
  def props(port: Int) = Props(classOf[ProviderActor], port)
}

class ProviderActor(port: Int) extends Actor {
  protected implicit def executor: ExecutionContext = context.dispatcher

  val providerUrl: String = "http://localhost:" + port + "/3rd/products"

  def receive = {
    case ProductRequest =>
      val response: HttpResponse[String] = ProviderHttp(providerUrl).asString
      if (response.isSuccess) {
        sender ! ProductResponse(providerUrl, parse(response.body))
      } else {
        sender ! ErrorResponse(response.statusLine, parse(response.body))
      }
  }
}