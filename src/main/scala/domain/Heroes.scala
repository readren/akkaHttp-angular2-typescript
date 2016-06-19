package domain

import scala.collection.mutable.{ Map => MMap }

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes

import controllers.AppExceptionWithStatusCode
import controllers.SpecifiesErrorCode
import scala.concurrent.Future

case class Hero(id: Int, name: String)

object HeroSrv {
  var sequencer: Int = 21

  val heroes: MMap[Int, Hero] = MMap(
    11 -> "Mr. Nice",
    12 -> "Narco",
    13 -> "Bombasto",
    14 -> "Celeritas",
    15 -> "Magneta",
    16 -> "RubberMan",
    17 -> "Dynama",
    18 -> "Dr IQ",
    19 -> "Magma",
    20 -> "Tornado").map { e => e._1 -> Hero(e._1, e._2) }

  def getAllHeroes: List[Hero] = this.heroes.values.toList

  def update(id: Int, hero: Hero): Future[Hero] = {
    if (id != hero.id)
      Future.failed(new AppExceptionWithStatusCode(StatusCodes.BadRequest))
    else if (heroes.isDefinedAt(hero.id))
      Future.successful {
        heroes += hero.id -> hero
        hero
      }
    else
      Future.failed(new AppExceptionWithStatusCode(StatusCodes.Gone))
  }

  def add(hero: Hero): Future[Hero] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    if (hero.id != 0)
      Future.failed(new AppExceptionWithStatusCode(StatusCodes.BadRequest))
    else if (heroes.exists(h => h._2.name == hero.name))
      Future.failed(new AppExceptionWithStatusCode(StatusCodes.Conflict))
    else Future {
      sequencer += 1
      val newHero = Hero(sequencer, hero.name)
      heroes += sequencer -> newHero
      newHero
    }
  }

  def remove(id: Int): Future[Int] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      heroes -= id
      id
    }
  }
}

