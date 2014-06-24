package models.SessionManager

import akka.actor.{ActorRef, Actor}
import models.SessionManager.userDataAccess.UsersDAO
import models.Mediator.Mediator.RegisterSelf
import scala.concurrent.duration._

/**
 * Created by basso on 10/6/14.
 */
// TODO: Incorporate Game State
object SessionManager {
  trait Directive
  case class NewRequest(user: String) extends Directive   // Returns an Either[String, Int]
  case class ConfirmRequest(token: Int) extends Directive

  case class TimeoutToken(t: Int)
}
import SessionManager._

class SessionManager(mediator: ActorRef) extends Actor {
  import scala.collection.mutable.{ Map => mMap }
  mediator ! RegisterSelf(classOf[Directive])

  val buff = mMap[Int, String]()
  var counter = 0
  import context.dispatcher

  val dbProxy = UsersDAO.getProxy(context)

  def receive = {
    case NewRequest(user) =>
      if (!buff.exists{case (_, u) => u == user} && dbProxy.available(user)) {
        counter += 1
        buff += Tuple2(counter,user)
        sender ! Right(counter)
        context.system.scheduler.scheduleOnce(10 seconds, self, TimeoutToken(counter))
      } else sender ! Left("User Already exists")

    case TimeoutToken(t) => buff.remove(t)

    case ConfirmRequest(token) =>
      buff.get(token) match {
        case None => sender ! Left("Token timed out")
        case Some(user) =>
          dbProxy.addUsers(user)
          buff.remove(token)
          sender ! Right(user)
      }
  }
}
