package models.Game

/**
 * Created by Anish'basso' on 07-04-2014.
 */
import play.api.libs.concurrent.Akka.system
import akka.actor.{TypedProps, TypedActor, Props}
import superActors.Mediator._
import models.GameManager.GameManager
import akka.actor.FSM.SubscribeTransitionCallBack
import play.api.Play.current
import models.ClientManager.Client
import models.GameManager.GameManager.GameConfiguration

object Game {
  private val mediator = system.actorOf(Props[Mediator], "Mediator")
  private val gameManager = system.actorOf(Props(classOf[GameManager], mediator), "GameManager")
  def clientProps = Client.props(mediator)_
  def configureGame(gc: GameConfiguration) = gameManager ! gc

  val gameAccess: GameAccess = TypedActor(system).typedActorOf(TypedProps(classOf[ManagerProxy],
      new ManagerProxy(mediator)), "ManagerProxy")

  gameManager ! SubscribeTransitionCallBack(TypedActor(system).getActorRefFor(gameAccess))
}
