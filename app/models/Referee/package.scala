package models

import play.api.libs.json.JsValue
import models.ClientManager.Client
import akka.actor.ActorRef

/**
 * Created by Anish'basso' on 6/4/14.
 */
package object Referee {
  type Ticket = Array[Array[Int]]
  type PrizeMap = Map[ClaimType, prizeConfig]
  type ClerkMap = Map[ClaimType, ActorRef]



  case class ClaimSuccess(user: String, claimType: ClaimType)
  case class ClerkDone(c: ClaimType)
}
