package models.TicketGenerator

import akka.actor.{FSM, ActorRef, Actor}
import models.{GameStart, GameEnd}
import superActors.Mediator._
import models.ClientManager.{ClientManager, Client}
import play.api.libs.json.Json

/**
 * Created by Anish'basso' on 3/4/14.
 *  needs a mediator for bi-way comm
 */
object TicketGenerator {
  sealed trait Data
  case object Uninitialized extends Data
  case class Balance(count: Int) extends Data

  trait Directive
  case class Setup(count: Int) extends Directive
  case object RequestNewTicket extends Directive
  case object RequestCount extends Directive
}

import TicketGenerator._
import models.{BiState, Active, Inactive}

class TicketGenerator(val mediator: ActorRef) extends Actor with FSM[BiState, Data] {
  startWith(Inactive, Uninitialized)
  mediator ! RegisterForReceive(self, classOf[Directive])
  import TicketLogic._

  /** Ticket Generator Active during the game standby time **/
  when(Inactive) {
    case Event(GameEnd, _) => goto (Active)
  }

  when (Active) {
    case Event(GameStart, _) =>
      goto (Inactive)

    case Event(RequestNewTicket, Balance(x)) =>
      if (x > 0) {
        mediator ! ClientManager.Broadcast(makeTicketsLeftMessage(x - 1))
        stay using Balance(x - 1) replying
          Client.TicketIssue( makeTicketIssueMessage(Json.toJson(create())))
      } else
        stay replying Client.TicketIssueFailure

    case Event(RequestCount, Balance(x)) =>
      stay replying Client.ServerMessage(makeTicketsLeftMessage(x))
  }

  whenUnhandled {
    case Event(Setup(count), _) =>
      if (count <= 0) throw new RuntimeException("TicketGenerator received invalid count")
      goto(Active) using Balance(count)
  }
}
