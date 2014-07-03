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
  //def newQueue = collection.mutable.Queue(Random.shuffle(1 to 90).toSeq :_*)
  def newQueue = collection.mutable.Queue(Random.shuffle(40 to 49).toSeq :_*)    // testing
  var numberBag = newQueue

  def receive = {
    case PickNewNumberNow =>
      sender ! numberBag.dequeue()
      if (numberBag.isEmpty)
        context.parent ! NoMoreNumbersLeft

    case Reset => numberBag = newQueue
  }
}
