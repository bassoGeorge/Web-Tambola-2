package models.Referee.Clerks

import akka.actor.ActorRef
import models.Referee.Ticket

/**
 * Created by Anish'basso' on 6/4/14.
 */
class CornersClerk (val tracker: ActorRef) extends Clerk {
  val claimType = models.ClaimType.Corners
  def checkClaim(t: Ticket) = exists(t(0)(0), t(0)(4), t(2)(0), t(2)(4))
}
