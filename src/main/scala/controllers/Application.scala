package controllers

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.settings.RoutingSettings
import akka.http.scaladsl.settings.ParserSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Application].getName))
  }
}

class Application extends Actor with ActorLogging {
  /**
   * change the vale of `index` here to use a different way of compilation and loading of the ts ng2 app.
   * index  :    does no ts compilation in advance. the ts files are download by the browser and compiled there to js.
   * index1 :    compiles the ts files to individual js files. Systemjs loads the individual files.
   * index2 :    add the option -DtsCompileMode=stage to your sbt task . F.i. 'sbt ~run -DtsCompileMode=stage' this will produce the app as one single js file.
   */
  val index = "views/index1.scala.html"

  val myExceptionHandler = ExceptionHandler {
    case _: ArithmeticException =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        complete(HttpResponse(StatusCodes.InternalServerError, entity = "We are ashamed! Something went wrong :("))
      }
  }

  val route: Route =
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
          self ! "shutdown" // TODO: repair this. Sometimes it fails.
          complete(HttpEntity("shuting down..")) // TODO repair, most times is not working
        }
    }

  override def preStart = {
    implicit val actorSystem = this.context.system
    implicit val materializer = ActorMaterializer()(actorSystem)
    val config = ConfigFactory.load()
    //    implicit val logger = Logging(actorSystem, getClass)
    implicit val rs = RoutingSettings(config)
    implicit val ps = ParserSettings(config)

    val flow = Route.handlerFlow(route)
    val fServerBinding = Http().bindAndHandle(flow, interface = "localhost", port = 9000)
  }

  //  override def posStop = {}

  def receive: Receive = {
    case "shutdown" => context.stop(self)
  }
}

