package models.Game

import akka.actor.ActorRef
import akka.actor.FSM.{Transition, CurrentState}
import akka.actor.TypedActor._
import akka.pattern._
import scala.util.{Failure, Success}
import models.ClientManager.ClientManager
import akka.actor.TypedActor
import scala.concurrent.duration._
import models.SessionManager.SessionManager
import models.{Stopped, GameState}
import models.GameManager.GameManager.GameConfiguration

/**
 * Created by Anish'basso' on 07-04-2014.
 */
class ManagerProxy(val mediator: ActorRef) extends GameAccess with Receiver {
  val context = TypedActor.context
  import context.dispatcher
  implicit val timeout = akka.util.Timeout(2 seconds)

  var _gameState: GameState = Stopped
  def onReceive(msg: Any, sender: ActorRef) = msg match {
    case CurrentState(_, state: GameState) => _gameState = state
    case Transition(_,_,to: GameState) => _gameState = to
  }

  def joinGame(username: String) = {
    (mediator ? SessionManager.NewRequest(username)).mapTo[Either[String, Int]]
  }

  def gameState = _gameState

  def configureGame(gc: GameConfiguration) {
    if (gameState != Stopped) throw new RuntimeException("Trying to configure game when it is not stopped")
    else mediator ! gc
  }
}
