package controllers

import play.api.mvc._
import models.Game.AdminForm._
import models.Game.Game.configureGame

/**
 * Created by basso on 17/6/14.
 */
object Admin extends Controller {
  def admin() = Action {
    implicit request =>
      Ok(views.html.admin(defaultAdmin))
  }

  // TODO: Check the game status and stuff
  def adminSubmit() = Action {
    implicit request =>
      adminForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.admin(formWithErrors)),
        userData => {
          configureGame(userData)
          Ok(views.html.testPage())
        }
      )
  }
}
