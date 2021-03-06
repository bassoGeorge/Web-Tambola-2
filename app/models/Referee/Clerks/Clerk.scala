package models.Referee.Clerks

import akka.actor.{FSM, ActorRef, Actor}
import models.Referee._
import play.api.libs.json._
import scala.concurrent.Await
import models.Tracker.Tracker
import scala.concurrent.duration._
import models.ClaimType
import models.ClientManager.Client
import akka.pattern._
import scala.util.{Failure, Success}
import Referee.Claim

/**
 * Created by Anish'basso' on 6/4/14.
 */

object Clerk {
  trait Data
  case class Prizes(p: Int, n: Int) extends Data
  case object Uninitialized extends Data

  case class Setup(perks: Int, count: Int)

}
import models.{BiState, Active, Inactive}
import Clerk._

// TODO: Referee needs pass on Tracker reference
trait Clerk extends Actor with FSM[BiState, Data] {
  val tracker: ActorRef
  val claimType: ClaimType

  implicit val system = context
  import context._
  implicit val timeout = akka.util.Timeout(2 seconds)

  def exists(args: Int*) =
    args.forall{ x => Await.result((tracker ? Tracker.CheckNum(x)).mapTo[Boolean], 3 seconds) }

  def checkClaim(t: Ticket): Boolean

    // Convert JSON ticket to an array
  def ticketify(ticket: JsValue): Ticket = {
    val basicData = (ticket \\ "number").map{_.as[Int]}.toArray
    val (row1, temp) = basicData splitAt 5
    val (row2, row3) = temp splitAt 5
    Array(row1, row2, row3)     // All seems in order
  }

  private[this] def makeMessage(s: String) (id: Int) = Json.obj (
    "kind" -> JsString("Claim"+s),
    "data" -> Json.obj(
      "claimType" -> claimType.toString,
      "ticketId" -> id
    )
  )

  val makeSMessage = makeMessage ("Success") _    // Success Message for the client
  val makeFMessage = makeMessage ("Failure") _    // Failure Message for the client
  val makeClientMessage =
    (data: JsValue) =>  (fn: (Int) => JsValue) =>
        Client.ServerMessage(fn((data \ "ticketId").as[Int]))

  startWith(Inactive, Uninitialized)
  when (Active) {
    case Event(Claim(clm), Prizes(perks, count)) =>
      if (checkClaim(ticketify(clm \ "ticket"))) {      // clm has to be the 'data' part
        (sender ? Client.GetUsername).onComplete {
          case Success(user) => parent ! ClaimSuccess(user.asInstanceOf[String], claimType, perks)
          case Failure(_) => parent ! ClaimSuccess("Unknown", claimType, perks)
        }
        sender ! makeClientMessage(clm)(makeSMessage)
        sender ! Client.UpdatePerks(perks)
        if (count == 1) goto(Inactive) using Uninitialized
        else stay using Prizes(perks, count - 1)
      } else stay replying makeClientMessage(clm)(makeFMessage)
  }

  when (Inactive) {
    case Event(Claim(clm),_) => stay replying makeClientMessage(clm)(makeFMessage)
  }

  whenUnhandled {
    case Event(Setup(perks, count), _) => goto (Active) using Prizes(perks, count)
  }

  onTransition {
    case Active -> Inactive => parent ! ClerkDone(claimType)
  }
}
