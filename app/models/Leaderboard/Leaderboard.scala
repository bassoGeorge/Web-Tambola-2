package models.Leaderboard

import akka.actor.{FSM, Actor, ActorRef}
import models.Mediator.Mediator
import models.{GameEnd, GameStart}
import play.api.libs.json._
import Json._
import models.ClientManager.ClientManager

/**
 * Created by Anish'basso' on 4/4/14.
 */
object Leaderboard {
  trait Data
  case object Empty extends Data
  case class LbData(data: List[LbEntry]) extends Data

  trait Directive
  case class Post(lbEntry: LbEntry) extends Directive

  private def createMessage(lst: List[LbEntry]) = Json.obj(
    "kind" -> "Leaderboard",
    "data" -> Json.toJson(lst)
  )
}
import Leaderboard._
import models.{BiState, Active, Inactive}

class Leaderboard(val mediator: ActorRef)
  extends Actor with FSM[BiState, Data] {

  mediator ! Mediator.RegisterSelf(classOf[Directive])
  startWith(Inactive, Empty)

  when (Inactive) {
    case Event(GameStart, _) => goto (Active) using LbData(List[LbEntry]())
  }

  when (Active) {
    case Event(Post(lbEntry), LbData(lst)) =>
      val nLst = (lbEntry :: lst).sorted
      mediator ! ClientManager.Broadcast(createMessage(nLst))
      stay using LbData(nLst)

    case Event(GameEnd, _) => goto (Inactive) using Empty
  }
}
