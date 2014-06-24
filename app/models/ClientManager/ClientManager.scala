package models.ClientManager

import akka.actor._
import play.api.libs.json._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import scala.concurrent.Await
import akka.pattern.ask
import scala.concurrent.duration._
import play.api.libs.iteratee.{Enumerator, Input, Done}
import org.joda.time.DateTime
import models.Mediator.Mediator
import models.{GameStart, GameEnd}
import akka.util.Timeout
import scala.Some
import models.ClientManager.ClientManager.Broadcast
import models.ClientManager.ClientManager.Setup
import akka.routing.Router
import akka.actor.Terminated
import akka.routing.ActorRefRoutee
import models.ClientManager.ClientManager.ManagerData

/**
 * Created by Anish'basso' on 31/3/14.
 * Responsible for handling clients
 */

/**
 * Clients are generated by the controller,
 * Once they are verified, they attach themeselves to the client-manager
 */
object ClientManager {
  trait State
  case object Running extends State
  case object Standby extends State

  trait Data
  case object Uninitialized extends Data
  case class ManagerData
    (initMessage: () => JsValue, router: Router) extends Data

  sealed trait Directive
  case class Setup(msg: JsValue, ticketPrice: Int, initialPerks: Int, startAt: DateTime ) extends Directive  // TODO: In futre, this may be used to allow variable ticket pricing between games
  case class Broadcast(msg: JsValue) extends Directive
  case object ReBroadcastStartMessage extends Directive
  object Joined extends Directive

  /*
  private val failedSocket: Socket = {
    val iteratee = Done[JsValue, Unit]((), Input.EOF)
    val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString("Cannot Connect, user already exists"))))
      .andThen(Enumerator.enumInput(Input.EOF))
    (iteratee, enumerator)
  }*/
  /**
   * Changed the implementation, ticketPrice will also be provided by the GameManager
   * hence, we have less work but GameManager has to send three items :
   *   1. details
   *   2. ticketPrice
   *   3. startAt
   */
  private def startMessageFn (initDetails: JsValue, ticketPrice: Int, startAt: DateTime): () => JsValue =
    () => Json.obj(
      "kind" -> "GameStartInfo",
      "data" -> Json.obj(
        "details" -> initDetails,
        "ticketPrice" -> ticketPrice,
        "timeLeft" -> org.joda.time.Seconds.secondsBetween(DateTime.now(), startAt).getSeconds
      )
    )
}
import ClientManager._

// Todo: I have removed the clientSet: Map[ActorRef, String] from the implementation, may need it later when implementing watch (most probably not)
class ClientManager (val mediator: ActorRef )
  extends Actor with FSM[State, Data] {

  implicit val system = context
  import system.dispatcher
  implicit val timeout = Timeout(3 seconds)

  var initialPerks = 300  // Default

  mediator ! Mediator.RegisterSelf(classOf[Directive])    // Register for messages at the mediator
  startWith(Standby, Uninitialized)

  when (Standby) {    // Game is in idle, waiting to start

      // First thing that the game manager does is send the initial configuration
    case Event(Setup(details, ticketPrice, initP, startAt), _) =>
      initialPerks = initP
      val newStartMessager = startMessageFn(details, ticketPrice, startAt)
      stateData match {
        case Uninitialized =>
          val router = Router (BroadcastRoutingLogic (), Vector[ActorRefRoutee] () )    // We need a router that broadcasts to all
          stay using ManagerData (newStartMessager, router)
        case md @ ManagerData(_,_) =>   // This happens at every iteration of the game
          self ! ReBroadcastStartMessage
          stay using md.copy(initMessage = newStartMessager)
      }

      // ReBroadcast happens at every iteration of the game
    case Event(ReBroadcastStartMessage, ManagerData(msg, router)) =>
      router.route(Client.InitMessage(msg(), Some(initialPerks)), ActorRef.noSender)
      stay

      // The Game Start trigger event
    case Event(GameStart, _: ManagerData) =>
      goto(Running)

    case Event(GameStart, Uninitialized) =>
      throw new IllegalStateException("ClientManager not initialized properly before game start")

      // The client attaches to the manager by sending the joined message
    case Event(Joined, md @ ManagerData(msg, router)) =>
      context watch sender            // We need to watch the client to manage the routee
      router.addRoutee(sender)        // make it a routee
      sender ! Client.InitMessage(msg())    // send it the initial message so that it can work
      stay
  }

  // Game is in running state
  when (Running) {
    case Event(GameEnd, _: ManagerData) =>
      goto(Standby)

    case Event(Broadcast(msg), ManagerData(_, router)) =>
      router.route (Client.ServerMessage(msg), self)
      stay
  }

  onTransition {
    case Standby -> Running =>
      nextStateData match {
        case ManagerData(_, router) => router.route(GameStart, ActorRef.noSender)
        case _ => throw new IllegalStateException("Invalid StateData on transition to Running")
      }

    case Running -> Standby =>
      nextStateData match {
        case ManagerData(_, router) => router.route(GameStart, ActorRef.noSender)
        case _ => throw new IllegalStateException("Invalid StateData on transition to Standby")
      }
  }

  whenUnhandled {
    case Event(Terminated(client), md @ ManagerData(_, router)) =>    // Any time a client disconnects, it need to be cleared off the router list
      router.removeRoutee(client)
      stay
  }
}