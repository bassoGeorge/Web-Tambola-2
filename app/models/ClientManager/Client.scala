package models.ClientManager

import akka.actor._
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json._
import play.api.libs.json.Json._
import scala.concurrent.ExecutionContext.Implicits.global
import models.{GameEnd, GameStart}
import models.TicketGenerator.TicketGenerator.{RequestCount, RequestNewTicket}
import models.Referee.Referee.Claim
import models.ClientManager.Client.ErrorMessage
import models.ClientManager.Client.TicketIssue
import models.Referee.Referee.Claim
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import models.SessionManager.SessionManager
import akka.pattern._

/**
 * Created by Anish'basso' on 18/3/14.
 */
object Client {
  /** FSM */
  trait State
  case object Born extends State
  case object Waiting extends State
  case object Playing extends State

  trait Data
  case object Uninitialized extends Data
  case class CurrentData(perks: Int, ticketPrice: Int) extends Data

  trait Message
  case class InitMessage(msg: JsValue, perks: Option[Int] = None) extends Message
  case class ServerMessage(msg: JsValue) extends Message
  case class ErrorMessage(msg: String) extends Message

  trait Directive
  case class UpdatePerks(n: Int) extends Directive
  case object GetUsername extends Directive
  case class TicketIssue(data: JsValue) extends Directive
  case object TicketIssueFailure extends Directive

  def props(mediator: ActorRef)(out: ActorRef) = Props(classOf[Client], out, mediator)
}
import Client._

class Client
(socket: ActorRef, mediator: ActorRef)
  extends Actor with FSM[State, Data] {

  import akka.util.Timeout
  import scala.concurrent.duration._

  implicit val timeout = Timeout(3 seconds)
  /*
  val socketIn = Iteratee.foreach[JsValue] { event =>
    (event \ "kind").as[String] match {
      case "TicketRequeust" => self ! RequestNewTicket
      case "Claim" => self ! Claim(event)
      case _ => self ! ErrorMessage(" ## Programmer Error, bad data received ## ")
    }
  } map {_ => context.stop(self) }*/

  //val (socketOut, socketOutChannel) = Concurrent.broadcast[JsValue]


  var username = ""
  var originalPerks = 300 // default
  def userData(perks: Int) = Json.obj(
      "kind" -> JsString("UserData"),
      "data" -> JsNumber(perks)
    )

  startWith(Born, Uninitialized)

  when (Born) {
    case Event(data: JsObject, Uninitialized) => data \ "token" match {
      case JsNumber(n) =>
        println("DEBUG: client sent token : "+n)
        (mediator ? SessionManager.ConfirmRequest(n.toInt)).mapTo[Either[String, String]].foreach {
          case Right(user) =>
            username = user
            socket ! Json.obj("connectionStatus" -> true)     // last step of the handshake
          case Left(_) =>
            socket ! Json.obj("connectionStatus" -> false)
            self ! PoisonPill
        }
        goto (Waiting)
      case _ => stay
    }
    /*case Event(GetSocket, Uninitialized) =>
      goto (Waiting) replying (socketIn, socketOut)*/
  }

  when (Waiting) {
    // InitMessage sent by client manager
    case Event(InitMessage(msg, perks), _) =>
      self ! ServerMessage(msg)
      val curPerks = stateData match {
        case Uninitialized =>
          originalPerks = perks.getOrElse(300)
          originalPerks

        case CurrentData(p, _) => p
      }
      self ! ServerMessage(userData(curPerks))
      mediator ! RequestCount
      stay using CurrentData(curPerks, (msg \\ "ticketPrice").head.as[Int])

    case Event(GameStart, CurrentData(_,_)) =>
      goto (Playing)

    case Event(RequestNewTicket, CurrentData(perks, ticketPrice)) =>
      if (perks >= ticketPrice)
        mediator ! RequestNewTicket
      else self ! ErrorMessage("Not enough perks to buy a new ticket")
      stay

    case Event(TicketIssue(ticket), CurrentData(perks, ticketPrice)) =>
      self ! ServerMessage(ticket)
      stay using CurrentData(perks - ticketPrice, ticketPrice)

    case Event(TicketIssueFailure, _) =>
      self ! ErrorMessage("Failed to issue ticket, probably because system is out of tickets")
      stay
  }

  when (Playing) {
    case Event(GameEnd, CurrentData(_,_)) =>
      goto (Waiting)

    case Event(e: Claim, _) =>
      mediator ! e
      stay
    case Event(UpdatePerks(count), CurrentData(p, tp)) =>
      self ! ServerMessage(userData(p+count))
      stay using CurrentData(p+count, tp)
  }

  onTransition{
    case Born -> Waiting =>
      mediator ! ClientManager.Joined

    case Waiting -> Playing =>
      self ! ServerMessage(Json.obj("kind" -> "GameStart"))

    case Playing -> Waiting =>
      val p = stateData.asInstanceOf[CurrentData].perks
      self ! ServerMessage(Json.obj(
        "kind" -> "GameEnd",
        "data" -> JsNumber(p - originalPerks)
      ))
      originalPerks = p
  }

  whenUnhandled{
    case Event(message: JsObject, _) =>
      message \ "kind" match {
        case JsString("TicketRequest") => self ! RequestNewTicket
        case JsString("Claim") => self ! Claim(message \ "data")
        case JsString("Test") =>    // For tests
          println("Debug: Client sent a Test message, replying")
          socket ! Json.obj(
          "kind" -> "Test",
          "data" -> JsString("We received ur message :> "+ message \ "data")
        )
      }; stay

    case Event(ErrorMessage(msg), _) =>
      self ! ServerMessage(Json.obj(
        "kind" -> "ErrorMessage",
        "data" -> msg
      ))
      stay

    case Event(ServerMessage(msg), _) =>
      socket ! msg
      stay

    case Event(GetUsername, _) => stay replying username
  }
}
