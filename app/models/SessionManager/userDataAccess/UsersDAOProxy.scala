package models.SessionManager.userDataAccess

import akka.actor.ActorRefFactory
import akka.pattern._
import akka.util.Timeout
import scala.concurrent._
import duration._
//import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.{Failure, Success}

/**
 * Created by basso on 9/6/14.
 */
class UsersDAOProxy(implicit system: ActorRefFactory) {
  import UsersDAO._
  implicit val timeout = Timeout(3 seconds)

  def addUsers(users: String*): Future[Boolean] = {
    val res: Future[Seq[(String, Boolean)]] = Future.sequence(
      for (user <- users.distinct) yield Future(user).zip((dao ? UserAvailable(user)).asInstanceOf[Future[Boolean]])
    )
    res.flatMap( (s) =>
      Future.sequence(
        s.collect{
        case (u, true) => (dao ? Add(u)).map{
          case Success(_) => true
          case Failure(_) => false
        }
      }).map(_.fold(true)(_&&_))
    )
  }

  def removeUsers(users: String*): Future[Boolean] = {
    Future.sequence(
      for (user <- users.distinct) yield (dao ? Remove(user)).map {
        case Success(_) => true
        case Failure(_) => false
    }).map{_.fold(true)(_&&_)}
  }

  def available(user: String) = {
    Await.result((dao ? UserAvailable(user)).asInstanceOf[Future[Boolean]], timeout.duration)
  }

  def list = Await.result((dao ? UserList).asInstanceOf[Future[List[String]]], timeout.duration)
}
