package models.GameManager

import models.Referee.{PrizeMap, prizeConfig}
import models.ClaimType._
import play.api.libs.json._
import org.joda.time.DateTime
import scala.concurrent.duration._
import akka.actor.ActorRef
import models.GameManager.GameManager.GameConfiguration
import models.ClientManager.ClientManager
import models.Referee.Referee
import models.TicketGenerator.TicketGenerator
import models.Announcer.Announcer

/**
 * Created by Anish'basso' on 07-04-2014.
 * This object contains various methods used by the Game manger to run its logic
 */

private[GameManager] object ManagerLogic {
    // create the prize configuration mapping for our referee
  def createPrizeConfigMap(tc: TotalNumbersConfig, pc: PrizeMoneyConfig): PrizeMap =
    Map(
      Line1 -> prizeConfig(tc.line, pc.line),
      Line2 -> prizeConfig(tc.line, pc.line),
      Line3 -> prizeConfig(tc.line, pc.line),
      BullsEye -> prizeConfig(tc.BullsEye, pc.BullsEye),
      Corners -> prizeConfig(tc.Corners, pc.Corners),
      Star -> prizeConfig(tc.Star, pc.Star),
      FullHouse -> prizeConfig(tc.FullHouse, pc.FullHouse)
    )

    // create the 'details' part of the initial client message
  def makeDetailsMessage(pm: PrizeMap): JsValue = JsObject(
    pm.toSeq.map{ case (c, pc) =>
      (c.toString, Json.obj("prize" -> pc.perks, "count" -> pc.num))}
  )

    // master method, create all configs and send them into the wild ~
  def createConfigAndSend(mediator: ActorRef, config: GameConfiguration) {

      // Methods and lazy vals for individual configurations
    def announcerConfig(np: NumberPickTimingConfig) =
      Announcer.PickTimeConfig(np.averageTime, np.maxOffset)
    def clientManagerConfig(tc: TicketConfig, tm: TimeConfig, pm: PrizeMap) =
      ClientManager.Setup(
        makeDetailsMessage(pm),
        tc.pricePerTicket,
        tc.initialPerks,
        DateTime.now().plusMinutes(tm.gameToStartIn)
      )
    def refereeConfig(pm: PrizeMap) = Referee.Setup(pm)
    def ticketGenConfig(tc: TicketConfig) = TicketGenerator.Setup(tc.ticketsAvailable)
    lazy val prizeConfigMap = createPrizeConfigMap(config.totalNumbers, config.prizeMoney)

      // Create and send
    mediator ! announcerConfig(config.numberPickTiming)
    mediator ! clientManagerConfig(config.ticketConfig, config.timeConfig, prizeConfigMap)
    mediator ! refereeConfig(prizeConfigMap)
    mediator ! ticketGenConfig(config.ticketConfig)
  }
}
