package models.GameManager
import models._
import models.Referee.Referee
import akka.actor._
import models.ClientManager.ClientManager
import models.Mediator._
import models.Announcer.Announcer
import models.Leaderboard.Leaderboard
import models.TicketGenerator.TicketGenerator
import models.Tracker.Tracker
import models.SessionManager.SessionManager

/**
 * Created by Anish'basso' on 6/4/14.
 */
object GameManager {

  trait Directive
  trait GameData

  // Actual Configuration Class
  case class GameConfiguration(
    totalNumbers: TotalNumbersConfig,
    prizeMoney: PrizeMoneyConfig,
    numberPickTiming: NumberPickTimingConfig,
    ticketConfig: TicketConfig,
    timeConfig: TimeConfig
  ) extends Directive with GameData

  case object Uninitialized extends GameData
  case object StartTheGame extends Directive
  case object PreStart extends Directive
}
import models.GameManager.GameManager._
import ManagerLogic._
import scala.concurrent.duration._
/**
 * The Heavy-weight game manager class
 * @param mediator: The Mediator actor which holds connections to all the main actors in the system
 */
class GameManager(val mediator: ActorRef) extends Actor with FSM[GameState, GameData] {
    // Setup against the mediator
  mediator ! RegisterForReceive(self, classOf[Directive])
  mediator ! RegisterBroadcastMessage(classOf[GlobalMessage])

  implicit val system = context
  import system.{dispatcher, actorOf, system => actorSys}

  /*----------- Creating actors ----------*/
    /* ------ Receiving colleagues ------ */
  mediator ! RegisterForReceive(
    actorOf(Props[ClientManager], "ClientManager"),
    classOf[ClientManager.Directive])

  mediator ! RegisterForReceive(
    actorOf(Props[SessionManager], "SessionManager"),
    classOf[SessionManager.Directive])

  val tracker = actorOf(Props[Tracker], "Tracker")      // Coz we need a reference to the tracker right now
  mediator ! RegisterForReceive(tracker, classOf[Tracker.Directive])

    /* -------- Other Colleagues ------- */
  val otherColleagues: List[Class[_]] = List(classOf[Announcer], classOf[Leaderboard], classOf[TicketGenerator])
  otherColleagues.foreach { x => actorOf(Props(x,mediator), x.getName)}

  val referee = actorOf(Props(classOf[Referee], mediator, tracker), "Referee")
  /* -------------------------------------- */

  startWith(Stopped, Uninitialized)
  when (Stopped) {
    case Event(gc: GameConfiguration, _) =>
      goto (Waiting) using gc
  }
  when (Waiting) {
    case Event(StartTheGame,_) => goto (Running)
    case Event(PreStart,gc @ GameConfiguration(_,_,_,_,TimeConfig(startIn, _))) =>
      createConfigAndSend(mediator, gc)   // configuration is sent every time a game starts
      actorSys.scheduler.scheduleOnce(startIn.minutes,self, StartTheGame)
      stay
  }
  when (Running) {
      // Game has ended, lets get ready for the next game if its there
    case Event(GameEnd, gc @ GameConfiguration(_,_,_,_,tc @ TimeConfig(_,n))) =>
      if (n == 1) goto(Stopped) using Uninitialized
      else goto(Waiting) using gc.copy(timeConfig = tc.copy(numberOfGames = n - 1))
  }

  onTransition{
    case _ -> Waiting =>    // Main transition
      nextStateData match {
        case gc: GameConfiguration => stateName match {
          case Stopped => self ! PreStart
          case Running => actorSys.scheduler.scheduleOnce(5 seconds, self, PreStart)
        }
        case Uninitialized => throw new IllegalStateException("Game manager went to 'Waiting' without proper configuration object"+stateData)
      }

    case Waiting -> Running =>
      mediator ! GameStart

    case Running -> Stopped =>
      mediator ! ClientManager.DisconnectClients

  }
}
