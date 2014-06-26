package models.Announcer

import akka.actor.{FSM, Actor}
import scala.util.Random
import scala.concurrent.duration.FiniteDuration
import models.Announcer.Announcer.PickTimeConfig

/**
 * Created by Anish'basso' on 5/4/14.
 * The implementation assumes that the MaxOffset and Average time will not change between games
 */
object Stopwatch {
  trait State
  case object Started extends State
  case object Paused extends State
  case object Stopped extends State

  case class Time(pc: PickTimeConfig, time: FiniteDuration)

  case object Start
  case object Stop
  case class MoveOn(time: FiniteDuration)
  val defaultPickTime = PickTimeConfig(15, 5)
}
import Stopwatch._
import scala.concurrent.duration._

class Stopwatch extends Actor with FSM[State, Time] {
  import context._
  def getNewTime(pc: PickTimeConfig) =
    (Random.nextInt(2 * pc.maxOffset + 1) + pc.averageTime - pc.maxOffset).seconds

  startWith(Stopped, Time(defaultPickTime, 0 second))

  when (Stopped) {
    case Event(pc: PickTimeConfig, _) => stay using Time(pc, 0 second)
    case Event(Start, Time(pc,_)) =>
      goto (Started) using Time(pc, getNewTime(pc))

    case Event(MoveOn(_),_) =>    // previously scheduled
      stay
  }

  when (Started) {
    case Event(MoveOn(d), t) =>
      goto (Paused) using t.copy(time = d)
  }

  when (Paused) {
    case Event(MoveOn(d), t) =>
      goto (Started) using t.copy(time = d)
  }

  whenUnhandled {
    case Event(Stop, t) =>
      goto (Stopped) using t.copy(time = 0 second)
  }

  onTransition{
    case _ -> Started =>
      val t = nextStateData.time
      val tLeft = t / 2
      if (stateName == Paused) parent ! PickNewNumberNow
      system.scheduler.scheduleOnce(tLeft, self, MoveOn(t - tLeft))

    case Started -> Paused =>
      val t = nextStateData.time
      parent ! NotifyTime(t)
      system.scheduler.scheduleOnce(t, self, MoveOn(getNewTime(stateData.pc)))
  }
}
