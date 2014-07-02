package models.Announcer

import akka.actor.{Props, ActorRef, FSM, Actor}
import superActors.Mediator._
import models.{GameEnd, GameStart}
import play.api.libs.json.Json
import models.ClientManager.ClientManager
import akka.pattern._
import scala.concurrent._
import duration._
import models.Tracker.Tracker

/**
 * Created by Anish'basso' on 5/4/14.
 * The announcer is responsible for the basic flow of the game
 *  It needs mediator, bi-way comm
 */
object Announcer {
  trait Directive
  case class PickTimeConfig(averageTime: Int, maxOffset: Int) extends Directive

  private def createMessage (m: String)(t: Int) = Json.obj(
    "kind" -> m,
    "data" -> t
  )

  private val timeLeftMessage = createMessage ("TimeLeftForNewNumber") _
  private val numberPickMessage = createMessage ("NewNumberPick") _
}
import Announcer._
import models.{BiState, Active, Inactive}

class Announcer (mediator: ActorRef)
  extends Actor with FSM[BiState, Unit] {

  mediator ! RegisterForReceive(self, classOf[Directive])
  implicit val system = context
  implicit val timeout = akka.util.Timeout(2 second)
  import system._

    // Sub workers for the announcer
  val stopwatch = actorOf(Props[Stopwatch], "Stopwatch")      // handles the timming
  val picker = actorOf(Props[Picker], "Picker")               // selects the number

  startWith(Inactive, Unit)
  when (Inactive) {
    case Event(m: PickTimeConfig,_) => stopwatch ! m; stay    // forward the configuration to the stopwatch
    case Event(GameStart,_) => goto (Active)
  }

  when (Active) {
    case Event(GameEnd, _) => goto (Inactive)
    case Event(PickNewNumberNow,_) =>   // sent by the stopwatch
      (picker ? PickNewNumberNow).mapTo[Int].onSuccess{
        case x =>   // we have the number, notify the clientManager and the tracker about it
          mediator ! Tracker.NumberPick(x)
          mediator ! ClientManager.Broadcast(numberPickMessage(x))
      }
      stay

    case Event(NotifyTime(d),_) =>    // advance notification of time remaining to a new number pick
      mediator ! ClientManager.Broadcast(timeLeftMessage(d.toSeconds.toInt))
      stay

    case Event(NoMoreNumbersLeft, _) =>   // were out, game end
      context.system.scheduler.scheduleOnce(3 seconds, mediator, GameEnd)   // give a bit of time to get things together
      goto (Inactive)
  }
  whenUnhandled (FSM.NullFunction)

  onTransition {
    case Inactive -> Active =>
      stopwatch ! Stopwatch.Start
    case Active -> Inactive =>
      stopwatch ! Stopwatch.Stop
      picker ! Picker.Reset
  }
}
