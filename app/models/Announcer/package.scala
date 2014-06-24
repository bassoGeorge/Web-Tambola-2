package models

import scala.concurrent.duration.FiniteDuration

/**
 * Created by Anish'basso' on 5/4/14.
 */
package object Announcer {
  trait innerMessage
  case class NotifyTime(time: FiniteDuration) extends innerMessage
  case object PickNewNumberNow extends innerMessage
  case object NoMoreNumbersLeft extends innerMessage
}
