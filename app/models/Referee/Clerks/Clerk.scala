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
  case class Prizes(n: Int) extends Data
  case object Uninitialized extends Data

  case class Setup(count: Int)

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

  def ticketify(ticket: JsValue): Ticket = {
    val basicData = (ticket \\ "number").toArray.map{_.as[Int]}
    val (row1, temp) = basicData splitAt 5
    val (row2, row3) = temp splitAt 5
    Array(row1, row2, row3)
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
    case Event(Claim(clm), Prizes(count)) =>
      if (checkClaim(ticketify(clm \ "ticket"))) {
        (sender ? Client.GetUsername).onComplete {
          case Success(user) => parent ! ClaimSuccess(user.asInstanceOf[String], claimType)
          case Failure(_) => parent ! ClaimSuccess("Unknown", claimType)
        }
        sender ! makeClientMessage(clm)(makeSMessage)
        if (count == 1) goto(Inactive) using Uninitialized
        else stay using Prizes(count - 1)
      } else stay replying makeClientMessage(clm)(makeFMessage)
  }

  when (Inactive) {
    case Event(Claim(clm),_) => stay replying makeClientMessage(clm)(makeFMessage)
  }

  whenUnhandled {
    case Event(Setup(count), _) => goto (Active) using Prizes(count)
  }

  onTransition {
    case Active -> Inactive => parent ! ClerkDone(claimType)
  }
}
