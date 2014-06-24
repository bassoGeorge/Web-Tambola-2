package models.TicketGenerator

import scala.util.Random
import play.api.libs.json._
import play.api.libs.json.JsArray
import scala.Some
import play.api.libs.json.JsNumber

/**
 * Created by Anish'basso' on 3/4/14.
 */
private[TicketGenerator] object TicketLogic {
  val randNumGenerator = new Random()

  private def rand (a: Int, b: Int): Int = {
    randNumGenerator.nextInt(b - a) + a
  }

  private val funList =
    List(
      () => rand(1, 10),
      () => rand(10, 20),
      () => rand(20, 30),
      () => rand(30, 40),
      () => rand(40, 50),
      () => rand(50, 60),
      () => rand(60, 70),
      () => rand(70, 80),
      () => rand(80, 91)
    )

  private def randDropSequence: List[Int] = {
    val scheme =
      if (randNumGenerator.nextBoolean()) List((0,2), (2,4), (4,7), (7,9))
      else List((0,2), (2,5), (5,7), (7,9))

    for ((i,j) <- scheme) yield rand(i,j)
  }

  def create(): Ticket = {
    val appliedFunList: List[List[() => Int]] =   // Create Matrix of actual functions to call
    (for {_ <- 1 to 3
         dropSeq = randDropSequence } yield {
      (for (i <- 0 until 9) yield {
        if (dropSeq.contains(i)) () => 0
        else funList(i)
      }).toList
    }).toList

    val row1 = appliedFunList(0) map {_()}   // Creating row1 an easy affair
    val row2 =
      for {i <- 0 until 9
           f = appliedFunList(1)(i)} yield {
        var ret = f()
        while (ret != 0 && ret == row1(i))
          ret = f()
        ret
      }
    val row3 =
      for {i <- 0 until 9
           f = appliedFunList(2)(i) } yield {
        var ret = f()                         // check if number repeats on any of the above rows
        while (ret != 0 && (ret == row1(i) || ret == row2(i)))
          ret = f()
        ret
      }

    List(
      row1,
      row2.toList,
      row3.toList
    ).map {_.map{
      case x if x > 0 => Some(x)
      case _ => None
    }}
  }

  implicit object TicketWriter extends Writes[Ticket] {
    def writes(t: Ticket): JsArray = {
      JsArray(
        t.map { lst => JsArray( lst.map {
          case Some(x) =>
            Json.obj("number" -> JsNumber(x))
          case None => Json.obj()   // Modified
        })}
      )
    }
  }

  def makeTicketsLeftMessage(count: Int) = Json.obj(
    "kind" -> "TicketsLeft",
    "data" -> count
  )

  def makeTicketIssueMessage(ticket: JsValue) = Json.obj(
    "kind" -> "TicketIssue",
    "data" -> ticket
  )
}
