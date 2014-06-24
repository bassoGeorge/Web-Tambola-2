package models.Announcer

import scala.util.Random
import akka.actor.Actor

/**
 * Created by Anish'basso' on 5/4/14.
 */

object Picker {
  case object Reset
}
import Picker._

class Picker extends Actor {
  def setup = collection.mutable.Queue(Random.shuffle(1 to 90).toSeq :_*)
  var numberBag = setup

  def receive = {
    case PickNewNumberNow =>
      sender ! numberBag.dequeue()
      if (numberBag.isEmpty)
        sender ! NoMoreNumbersLeft

    case Reset => numberBag = setup
  }
}
