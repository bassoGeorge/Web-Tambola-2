package models.Game

/**
 * Created by basso on 17/6/14.
 */
import models.GameManager._
import GameManager.GameConfiguration
import play.api.data._
import play.api.data.Forms._

object AdminForm {
  private val sNum = number(min = 1)
  val adminForm: Form[GameConfiguration] = Form(
    mapping(
      "totalNumbers" -> mapping (
        "line" -> sNum,
        "BullsEye" -> sNum,
        "Corners" -> sNum,
        "Star" -> sNum,
        "FullHouse" -> sNum
      )(TotalNumbersConfig.apply)(TotalNumbersConfig.unapply),

      "prizeMoney" -> mapping (
        "line" -> number(min = 200),
        "BullsEye" -> number(min = 100),
        "Corners" -> number(min = 100),
        "Star" -> number(min = 300),
        "FullHouse" -> number(min = 400)
      )(PrizeMoneyConfig.apply)(PrizeMoneyConfig.unapply),

      "numberPickTiming" -> mapping(
        "averageTime" -> number(min = 5),
        "maxOffset" -> number(min = 0)
      )(NumberPickTimingConfig.apply)(NumberPickTimingConfig.unapply),

      "ticketConfig" -> mapping(
        "ticketsAvailable" -> number(min = 10),
        "pricePerTicket" -> number(min = 100),
        "initialPerks" -> number(min = 300)
      )(TicketConfig.apply)(TicketConfig.unapply),

      "timeConfig" -> mapping(
        "gameToStartIn" -> number(min = 1),
        "numberOfGames" -> number(min = 1)
      )(TimeConfig.apply)(TimeConfig.unapply)
    )(GameConfiguration.apply)(GameConfiguration.unapply)
  )

  val defaultAdmin = adminForm.fill( GameConfiguration(
    TotalNumbersConfig(1,1,1,1,1),
    PrizeMoneyConfig(200,100,100,300,400),
    NumberPickTimingConfig(6, 1),
    TicketConfig(100, 100, 400),
    TimeConfig(1, 1)
  ))
}
