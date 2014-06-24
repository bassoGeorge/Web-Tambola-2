package models.Referee.Clerks

import akka.actor.ActorRef
import models.Referee.Ticket

/**
 * Created by Anish'basso' on 6/4/14.
 */
class FullHouseClerk(val tracker: ActorRef) extends Clerk {
  val claimType = models.ClaimType.FullHouse
  def checkClaim(t: Ticket) = t.forall(_.forall(exists(_)))
}
