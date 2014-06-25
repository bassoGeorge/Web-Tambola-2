
/**
 * Created by Anish'basso' on 3/4/14.
 * Contains system wide declaration and types
 */

package object models {
  trait GlobalMessage
  case object GameStart extends GlobalMessage   // Can only be issued by the Game Manager
  case object GameEnd extends GlobalMessage     // Can be issued by either the announcer Or the referee

  trait BiState
  case object Active extends BiState
  case object Inactive extends BiState

  trait GameState
  case object Waiting extends GameState
  case object Running extends GameState
  case object Stopped extends GameState

  /** Claim system **/
  object ClaimType extends Enumeration {
    type ClaimType = Value
    val BullsEye,
        Corners,
        Line1,
        Line2,
        Line3,
        Star,
        FullHouse = Value
  }
  type ClaimType = ClaimType.ClaimType
  
  import ClaimType._
  lazy val claimNormalise: Map[ClaimType, String] = Map(
    Line1 -> "Line 1",
    Line2 -> "Line 2",
    Line3 -> "Line 3",
    Corners -> "Corners",
    BullsEye -> "Bull's eye",
    FullHouse -> "Full house"
  )

  import play.api.libs.json._
  import Json._

  implicit object ClaimTypeReader extends Reads[ClaimType] {
    def reads(o: JsValue) =
      ClaimType.values.find{_.toString == o.as[String]} match {
        case Some(r) => JsSuccess(r)
        case None => JsError()
      }
  }
}
