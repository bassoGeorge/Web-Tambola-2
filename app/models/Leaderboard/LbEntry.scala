package models.Leaderboard
import models._

/**
 * Created by Anish'basso' on 4/4/14.
 */
case class LbEntry(claimType: ClaimType, user: String, winning: Int)
  extends Ordered[LbEntry] {
  def compare(that: LbEntry) =
    if (that.claimType == this.claimType) 0
    else if (that.claimType > this.claimType) -1
    else 1
}
