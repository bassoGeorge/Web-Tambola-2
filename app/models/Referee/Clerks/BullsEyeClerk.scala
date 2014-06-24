package models.Referee.Clerks

import akka.actor.ActorRef
import models.Referee.Ticket

/**
 * Created by Anish'basso' on 6/4/14.
 */

class BullsEyeClerk (val tracker: ActorRef) extends Clerk {
  val claimType = models.ClaimType.BullsEye
  def checkClaim(t: Ticket): Boolean = exists(t(1)(2))
}
