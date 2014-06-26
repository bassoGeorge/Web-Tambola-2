package models.Tracker

import akka.actor.{ActorRef, FSM, Actor}
import scala.collection.SortedSet
import models.Mediator.Mediator
import models.{GameEnd, GameStart}

/**
 * Created by Anish'basso' on 6/4/14.
 * Keeps track of the numbers picked during the course of the game
 * is a single point of reference for deciding whether a given number was selected or not
 */
object Tracker {
  trait Data
  case object Uninitialized extends Data
  case class Numbers(num: SortedSet[Int]) extends Data

  trait Directive
  case class CheckNum(n: Int) extends Directive
  case class NumberPick(n: Int) extends Directive
}
import Tracker._
import models.{BiState, Active, Inactive}

class Tracker(val mediator: ActorRef) extends Actor with FSM[BiState, Data] {
  mediator ! Mediator.RegisterSelf(classOf[Directive])

  startWith(Inactive, Uninitialized)

  when (Inactive) {
    case Event(GameStart, _) => goto (Active) using Numbers(SortedSet())
  }

  when (Active) {
    case Event(GameEnd, _) => goto (Inactive) using Uninitialized
    case Event(CheckNum(x), Numbers(num)) => stay replying num.exists(_==x)
    case Event(NumberPick(x), Numbers(num)) => stay using Numbers(num + x)
  }
}
