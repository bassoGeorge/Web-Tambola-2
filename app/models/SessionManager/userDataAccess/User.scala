package models.SessionManager.userDataAccess

/**
 * Created by basso on 9/6/14.
 */
import scala.slick.driver.H2Driver.simple._

case class User(name: String, id: Option[Int] = None)

class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME")
  def * = (name, id.?) <> (User.tupled, User.unapply)
}
