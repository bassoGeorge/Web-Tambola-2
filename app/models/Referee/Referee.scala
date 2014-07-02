package models.Referee

import akka.actor.{Props, FSM, Actor, ActorRef}
import models.ClaimType.ClaimType
import superActors.Mediator._
import models.{ClaimType, GameStart}
import models.Referee.Clerks.Reset
import models.ClaimType.ClaimType
import Clerks._
import models._
import play.api.libs.json._
import models.Leaderboard.{Leaderboard, LbEntry}
import models.ClientManager.Client
import models.ClientManager.ClientManager

/**
 * Created by Anish'basso' on 6/4/14.
 * Currently, the whole referee system is made under the assumption that the
 * Number of prizes and the winnings of each will be constant across multiple games
 */

/**
 * Claim api :
 * { kind: "Claim",
 *   data: {
 *      claimType: String,
 *      ticketId: Number,
 *      ticket: <ticket_obj>,
 *   }
 * }
 * Referee gets the 'data' part
 */

object Referee {
  trait Data
  case object Uninitialized extends Data
  case class RefereeData (prizeConfigMap: PrizeMap, activeClerks: Int) extends Data

  trait Directive
  case class Claim(data: JsValue) extends Directive
  case class Setup(prizeConfigMap: PrizeMap) extends Directive
}
import Referee._
import models.ClaimType._
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy

class Referee(val mediator: ActorRef, val tracker: ActorRef)
  extends Actor with FSM[BiState, Data]
{
  mediator ! RegisterForReceive(self, classOf[Referee.Directive])
  implicit val system = context
  import system._

  val clerkMap: ClerkMap = Map(
    Line1 -> actorOf(Props(classOf[LineClerk], tracker, 1), "Line1Clerk"),
    Line2 -> actorOf(Props(classOf[LineClerk], tracker, 2), "Line2Clerk"),
    Line3 -> actorOf(Props(classOf[LineClerk], tracker, 3), "Line3Clerk"),
    BullsEye -> actorOf(Props(classOf[BullsEyeClerk], tracker), "BullsEyeClerk"),
    Corners -> actorOf(Props(classOf[CornersClerk], tracker), "CornersClerk"),
    Star -> actorOf(Props(classOf[StarClerk], tracker), "StarClerk"),
    FullHouse -> actorOf(Props(classOf[FullHouseClerk], tracker), "FullHouseClerk")
  )

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: Exception => Resume   // We do not want the state data of clerks to be thrown away
      case t =>
        super.supervisorStrategy.decider.apply(t)
    }

  /** Working */
  startWith(Inactive, Uninitialized)
  when (Inactive) {
    case Event(Setup(prizeConfigMap), _) =>
      stay using RefereeData(prizeConfigMap, 7)
    case Event(GameStart, rd @ RefereeData(_,_)) => goto (Active) using rd.copy(activeClerks = 7)
  }

  when (Active) {
    case Event(ClerkDone(claimType), rd @ RefereeData(_, activeClerks)) =>
      mediator ! ClientManager.Broadcast(Json.obj("kind" -> "PrizeDepleted", "data" -> claimType.toString))
      if (activeClerks == 1) {
        mediator ! GameEnd
        goto(Inactive)
      } else stay using rd.copy(activeClerks = activeClerks - 1)

    case Event(ClaimSuccess(user, claimType, perks), RefereeData(_, _))=>
      mediator ! Leaderboard.Post(LbEntry(claimType, user, perks))
      stay

    case Event(GameEnd, _) => goto(Inactive)
  }

  whenUnhandled {
    case Event(c @ Claim(clm), _) =>
      clerkMap((clm \ "claimType").as[ClaimType]).tell(c, sender)   // c : {claimType:, ticketId:, ticket: }
      stay
  }

  onTransition {
    case Inactive -> Active =>
      nextStateData match {
        case RefereeData(prizeConfigMap, _) =>
          clerkMap.foreach{case (ct, a) => a ! Clerk.Setup(prizeConfigMap(ct).perks, prizeConfigMap(ct).num)}
        case _ => throw new RuntimeException("Invalid data for Referee on transition to Active")
      }
  }
}
