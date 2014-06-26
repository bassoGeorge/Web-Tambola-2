package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, JsString, Json}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def test = Action {
    Ok(views.html.testPage())
  }
  import models.Game._
  import models.Waiting

  def checkStatus() = Action { implicit request =>
    Ok(Json.obj("status" -> Game.gameAccess.gameState.toString))
  }

  def joinGame(username: Option[String]) = Action.async { implicit request =>
    username match {
      case Some(user) if Game.gameAccess.gameState == Waiting =>
        Game.gameAccess.joinGame(user).map {
          case Right(token) => Ok(Json.stringify(Json.obj("token" -> token)))
          case Left(errorMsg) => Ok(JsString(errorMsg))
        }
      case None =>
        Future.successful(BadRequest("Need a valid username"))
      case _ =>
        Future.successful(BadRequest("Cannot authorise user join while game is "+Game.gameAccess.gameState ))
    }
  }

  def connectJs() = Action { implicit request =>
    Ok(views.js.connect(request))
  }

  def confirmJoin() = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      Game.clientProps(out)
  }
}