package controllers

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.directives.LoggingMagnet.forMessageFromFullShow
import akka.http.scaladsl.server.directives.OnSuccessMagnet.apply
import akka.http.scaladsl.settings.ParserSettings
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import domain.Hero
import domain.HeroSrv
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.model.StatusCodes

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Application].getName))
  }
}

// Required to protect against JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
case class ArrayWrapper[T](wrappedArray: T)

// collect your json format instances into a support trait:
trait JsonSupport extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport with DefaultJsonProtocol {
  implicit val heroFormat = jsonFormat2(Hero)
  implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T])
}

class Application extends Actor with ActorLogging with JsonSupport {

  /**
   * change the value of `index` here to use a different way of compilation and loading of the ts ng2 app.
   * index  :    does no ts compilation in advance. the ts files are download by the browser and compiled there to js.
   * index1 :    compiles the ts files to individual js files. Systemjs loads the individual files.
   * index2 :    add the option -DtsCompileMode=stage to your sbt task . F.i. 'sbt ~run -DtsCompileMode=stage' this will produce the app as one single js file.
   */
  private val index = "index.html"

  private val port = 9001

  private var fServerBinding: Future[Http.ServerBinding] = _

  override def preStart = {
    implicit val actorSystem = this.context.system
    implicit val materializer = ActorMaterializer.create(actorSystem)
    implicit val rs = RoutingSettings(actorSystem)
    implicit val ps = ParserSettings(actorSystem)

    val indexRoute: Route = (pathSingleSlash | path("heroes" | "dashBoard")  | pathPrefix("detail" /))  { 
      getFromResource(index)
    }

    val heroRoute: Route =
      pathPrefix("app" / "heroes") {
        handleExceptions(exceptionHandler) {
          get {
            complete(ArrayWrapper(HeroSrv.getAllHeroes))
          } ~
            put {
              path(IntNumber) { id =>
                entity(as[Hero]) { hero =>
                  onSuccess(HeroSrv.update(id, hero)) { _ =>
                    complete("updated")
                  }
                }
              }
            } ~
            post {
              pathEnd {
                entity(as[Hero]) { hero =>
                  onSuccess(HeroSrv.add(hero)) { _ =>
                    complete("added")
                  }
                }
              }
            } ~
            delete {
              path(IntNumber) { id =>
                onSuccess(HeroSrv.remove(id)) { _ =>
                  complete("deleted")
                }
              }
            } ~
            complete(StatusCodes.MethodNotAllowed)
        }
      }

    val libAndAssetsRoute: Route =
      pathPrefix("lib" | "assets") { // TODO: put libraries and assets in separate folders. 
        getFromResourceDirectory("") ~ complete(StatusCodes.NotFound)
      }

    val shutdownRoute: Route =
      path("shutdown") { // TODO: add a button in the presentation to shut down. 
        self ! "shutdown"
        complete(HttpEntity("shuting down.."))
      }

    /**Produces a log entry for every RouteResult. The log entry includes the request URI */
    def logAccess(innerRoute: Route): Route = {
      def toLogEntry(marker: String, f: Any => String) = (r: Any) => LogEntry(marker + f(r), akka.event.Logging.InfoLevel)
      extractRequest { request =>
        logResult(toLogEntry(s"${request.method.name} ${request.uri} ==> ", {
          case c: RouteResult.Complete => c.response.status.toString()
          case x                       => s"unknown response part of type ${x.getClass}"
        }))(innerRoute)
      }
    }

    val compoundRoute: Route =
      logAccess {
        shutdownRoute ~
          encodeResponse {
            heroRoute ~
              libAndAssetsRoute ~
              indexRoute
          }

      }

    val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(compoundRoute)
    fServerBinding = Http().bindAndHandle(routeFlow, interface = "localhost", port = port)
    log.info(s"listening to http://localhost:$port")
  }

  override def postStop = {
    fServerBinding.onSuccess { case sb => sb.unbind() }(context.dispatcher)
  }

  override def receive: Receive = {
    case "shutdown" => context.stop(self)
  }

  val exceptionHandler = ExceptionHandler {
    case s: SpecifiesErrorCode =>
      extractUri { uri =>
        log.warning("Request to {} could not be handled normally", uri)
        complete(HttpResponse(s.code))
      }
  }

}

trait SpecifiesErrorCode { //self:Exception =>
  val code: StatusCode
}

class AppExceptionWithStatusCode(val code: StatusCode) extends Exception with SpecifiesErrorCode
