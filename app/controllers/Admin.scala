package controllers

import play.api.mvc._
import models.Game.AdminForm._
import models.Game.Game._
import models.Game.Game.gameAccess
import models._

/**
 * Created by basso on 17/6/14.
 */
object Admin extends Controller {
  def admin() = Action {
    implicit request =>
      if (gameAccess.gameState == Stopped)
        Ok(views.html.admin(defaultAdmin))
      else
        BadRequest("Game already running") // TODO: implement watch screen here
  }

  // TODO: Check the game status and stuff
  def adminSubmit() = Action {
    implicit request =>
      adminForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.admin(formWithErrors)),
        userData => {
          configureGame(userData)
          Ok(views.html.index())
        }
      )
  }
}
