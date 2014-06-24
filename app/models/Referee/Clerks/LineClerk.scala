package models.Referee.Clerks

/**
 * Created by Anish'basso' on 6/4/14.
 */
import models._
import ClaimType._
import Referee._
import akka.actor.ActorRef

class LineClerk(val tracker: ActorRef, val lNum: Int) extends Clerk {
  val claimType = ClaimType.values.find{_.toString == "Line"+lNum }.getOrElse{Line1}
  def checkClaim(t: Ticket): Boolean = exists( t(lNum - 1):_* )
}
