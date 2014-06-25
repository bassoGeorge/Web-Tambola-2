package models

/**
 * Created by Anish'basso' on 4/4/14.
 */
import play.api.libs.json._
import Json._

package object Leaderboard {

  import ClaimType._
  lazy val cPmap: Map[ClaimType, String] = Map(
    Line1 -> "Line 1",
    Line2 -> "Line 2",
    Line3 -> "Line 3",
    Corners -> "Corners",
    BullsEye -> "Bull's eye",
    FullHouse -> "Full house"
  )
  implicit object LbEntryWrites extends Writes[LbEntry] {
    def writes(that: LbEntry): JsValue = Json.obj(
      "prize" -> cPmap(that.claimType),
      "username" -> that.user,
      "winning" -> that.winning
    )
  }
}
