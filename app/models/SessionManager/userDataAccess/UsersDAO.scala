package models.SessionManager.userDataAccess

import akka.actor.{Props, ActorRefFactory, ActorRef, Actor}
import scala.slick.driver.H2Driver.simple._
import scala.util.Try

//import scala.concurrent.duration._

/**
 * Created by basso on 9/6/14.
 * Data Access Object for simple user registrations
 */
object UsersDAO {
  trait Directive
  case class Add(username: String) extends Directive
  case class Remove(username: String) extends Directive
  case class UserAvailable(username: String) extends Directive
  object UserList extends Directive

  private var _dao: ActorRef = null

  private[userDataAccess] def dao(implicit system: ActorRefFactory): ActorRef =
    if (_dao == null) {
      _dao = system.actorOf(Props[UsersDAO], "UsersDAO")
      _dao
    } else _dao

  def getProxy(implicit system: ActorRefFactory) = new UsersDAOProxy
}
import UsersDAO._

class UsersDAO extends Actor {
  val db = Database.forURL("jdbc:h2:mem:UsersDB;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  val users = TableQuery[UsersTable]
  //implicit val timeout = new Timeout(3 seconds)
  db.withSession { implicit session =>
    users.ddl.create
  }

  def receive = {
    case Add(username) => db.withSession{ implicit session =>
      sender ! Try(users += User(username))
    }

    case Remove(username) => db.withSession{ implicit session =>
      sender ! Try(users.filter{_.name === username }.delete)
    }

    case UserAvailable(username) => db.withSession{ implicit session =>
      sender ! users.filter{_.name === username }.list.isEmpty
    }

    case UserList => db.withSession{ implicit session =>
      sender ! users.list.map{_.name}
    }
  }
}
