package models

import models.Referee.prizeConfig

/**
 * Created by Anish'basso' on 6/4/14.
 * Contains all sub-configuration classes for the game manager
 * The main configuration resides in GameManager
 */
package object GameManager {

  // Number of each prizes
  case class TotalNumbersConfig(
    line: Int,
    BullsEye: Int,
    Corners: Int,
    Star: Int,
    FullHouse: Int)

  // winnings of each prize
  case class PrizeMoneyConfig(
    line: Int,
    BullsEye: Int,
    Corners: Int,
    Star: Int,
    FullHouse: Int)

  // speed of the game
  case class NumberPickTimingConfig(
    averageTime: Int,
    maxOffset: Int)

  // tickets configuration
  case class TicketConfig(
    ticketsAvailable: Int,
    pricePerTicket: Int,
    initialPerks: Int)

  // Delay and repetition of game
  case class TimeConfig(
    gameToStartIn: Int,
    numberOfGames: Int)
}
