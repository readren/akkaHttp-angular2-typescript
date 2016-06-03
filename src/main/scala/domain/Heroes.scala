package domain

import scala.collection.mutable.{ Map => MMap }

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes

import controllers.AppExceptionWithStatusCode
import controllers.SpecifiesErrorCode

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

  def update(id: Int, hero: Hero): Unit = {
    if (id != hero.id)
      throw new AppExceptionWithStatusCode(StatusCodes.BadRequest)
    else if (heroes.isDefinedAt(hero.id))
      heroes += hero.id -> hero
    else
      throw new AppExceptionWithStatusCode(StatusCodes.Gone)
  }

  def add(hero: Hero): Hero = {
    if (hero.id != 0)
      throw new AppExceptionWithStatusCode(StatusCodes.BadRequest)
    else if (heroes.exists(h => h._2.name == hero.name))
      throw new AppExceptionWithStatusCode(StatusCodes.Conflict)
    else {
      sequencer += 1
      val newHero = Hero(sequencer, hero.name)
      heroes += sequencer -> newHero
      newHero
    }
  }

  def remove(id: Int): Unit = {
    heroes -= id
  }
}

