package controllers

import scala.concurrent.Future

import com.typesafe.config.ConfigFactory

import akka.NotUsed
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpEntity.apply
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.encodeResponse
import akka.http.scaladsl.server.Directives.enhanceRouteWithConcatenation
import akka.http.scaladsl.server.Directives.extractUri
import akka.http.scaladsl.server.Directives.getFromResource
import akka.http.scaladsl.server.Directives.getFromResourceDirectory
import akka.http.scaladsl.server.Directives.handleExceptions
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.pathSingleSlash
import akka.http.scaladsl.server.Directives.segmentStringToPathMatcher
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ParserSettings
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Application].getName))
  }
}

class Application extends Actor with ActorLogging {

  private var fServerBinding: Future[Http.ServerBinding] = _

  override def preStart = {
    implicit val actorSystem = this.context.system
    implicit val materializer = ActorMaterializer()(actorSystem)

    val config = ConfigFactory.load()
    implicit val rs = RoutingSettings(config)
    implicit val ps = ParserSettings(config)

    val flow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(route)
    fServerBinding = Http().bindAndHandle(flow, interface = "localhost", port = 9000)
  }

  override def postStop = {
    fServerBinding.onSuccess { case sb => sb.unbind() }(context.dispatcher)
  }

  private val myExceptionHandler = ExceptionHandler {
    case e =>
      extractUri { uri =>
        log.error(s"Request to $uri could not be handled normally.", e)
        complete(HttpResponse(StatusCodes.InternalServerError, entity = "We are ashamed! Something went wrong :("))
      }
  }

  /**
   * change the vale of `index` here to use a different way of compilation and loading of the ts ng2 app.
   * index  :    does no ts compilation in advance. the ts files are download by the browser and compiled there to js.
   * index1 :    compiles the ts files to individual js files. Systemjs loads the individual files.
   * index2 :    add the option -DtsCompileMode=stage to your sbt task . F.i. 'sbt ~run -DtsCompileMode=stage' this will produce the app as one single js file.
   */
  private val index = "views/index1.scala.html"
  private val route: Route =
    handleExceptions(myExceptionHandler) {
      encodeResponse {
        pathSingleSlash {
          getFromResource(index)
        } ~
          pathPrefix("lib" | "assets") { // TODO: put libraries and assets in separate folders. 
            getFromResourceDirectory("")
          }
      } ~
        path("shutdown") { // TODO: add a button in the presentation to shut down. 
          self ! "shutdown"
          complete(HttpEntity("shuting down.."))
        }
    }

  def receive: Receive = {
    case "shutdown" =>
      import context.dispatcher
      log.info("disconecting..")
      fServerBinding.onSuccess {
        case sb =>
          log.info("shuting down...")
          sb.unbind().onComplete { _ => context.stop(self) }
      }
      
  }
}

