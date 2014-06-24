package models.Game

import scala.concurrent.Future
import models.GameManager.GameManager.GameConfiguration
import models.GameState

/**
 * Created by Anish'basso' on 07-04-2014.
 */
trait GameAccess {
  // To Join a game
  def joinGame(user: String): Future[Either[String, Int]]

  // Inquire the state of the server
  def gameState: GameState

  def configureGame(gc: GameConfiguration): Unit
}
