package models

/**
 * Created by Anish'basso' on 4/4/14.
 */
import play.api.libs.json._
import Json._

package object Leaderboard {

  implicit object LbEntryWrites extends Writes[LbEntry] {
    def writes(that: LbEntry): JsValue = Json.obj(
      "prize" -> claimNormalise(that.claimType),
      "username" -> that.user,
      "winning" -> that.winning
    )
  }
}
