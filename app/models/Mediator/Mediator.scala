package models.Mediator

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable.ListBuffer

/**
 * Created by Anish'basso' on 2/4/14.
 */
object Mediator {
  case class RegisterSelf(directiveClass: Class[_])
  case class RegisterGlobal(directiveClass: Class[_])
}
import Mediator._

class Mediator extends Actor {
  val cMap = collection.mutable.Map[Class[_], ActorRef]()
  val globalMessages = ListBuffer[Class[_]]()

  def receive = {
    case RegisterSelf(dClass) => cMap.update(dClass, sender)
    case RegisterGlobal(dClass) => globalMessages += dClass

    case message =>
      if (globalMessages exists { _.isAssignableFrom(message.getClass) })
        cMap.values.filterNot(_ == sender).foreach(_.tell(message, sender))
      else
        cMap.filterKeys(_.isAssignableFrom(message.getClass)).values.foreach(_.tell(message, sender))
  }
}
