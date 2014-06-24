package models.Announcer

import akka.actor.{Props, ActorRef, FSM, Actor}
import models.Mediator.Mediator
import models.{GameEnd, GameStart}
import play.api.libs.json.Json
import models.ClientManager.ClientManager
import akka.pattern._
import scala.concurrent._
import duration._

/**
 * Created by Anish'basso' on 5/4/14.
 * The announcer is responsible for the basic flow of the game
 */
object Announcer {
  trait Directive   // stub trait to register self with mediator
  case class PickTimeConfig(averageTime: Int, maxOffset: Int) extends Directive

  private def messageCreate (m: String)(t: Int) = Json.obj(
    "kind" -> m,
    "data" -> t
  )

  private val timeLeftMessage = messageCreate ("TimeLeftForNewNumber") _
  private val numberPickMessage = messageCreate ("NewNumberPick") _
}
import Announcer._
import models.{BiState, Active, Inactive}

class Announcer (mediator: ActorRef)
  extends Actor with FSM[BiState, Unit] {

  mediator ! Mediator.RegisterSelf(classOf[Directive])
  implicit val system = context
  implicit val timeout = akka.util.Timeout(2 second)
  import system._

    // Sub workers for the announcer
  val stopwatch = actorOf(Props[Stopwatch], "Stopwatch")
  val picker = actorOf(Props[Picker], "Picker")

  startWith(Inactive, Unit)
  when (Inactive) {
    case Event(m: PickTimeConfig,_) => stopwatch ! m; stay
    case Event(GameStart,_) => goto (Active)
  }

  when (Active) {
    case Event(GameEnd, _) => goto (Inactive)
    case Event(PickNewNumberNow,_) =>
      (picker ? PickNewNumberNow).map { x => ClientManager.Broadcast(numberPickMessage(x.asInstanceOf[Int])) } pipeTo mediator
      stay

    case Event(NotifyTime(d),_) =>
      mediator ! ClientManager.Broadcast(timeLeftMessage(d.toSeconds.toInt))
      stay

    case Event(NoMoreNumbersLeft, _) =>
      mediator ! GameEnd
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
