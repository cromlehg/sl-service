package models

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Date

import controllers.AppConstants
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

case class Reward(
  amount: String,
  id: Long,
  lotteryAddress: String,
  lotteryIndex: Long,
  ownerAddress: String,
  ownerId: Option[Long],
  registered: Long,
  roomId: Long,
  ticketNumber: Long,
  txHash: Option[String],
  owner: Option[Account],
) {

  lazy val createdPrettyTime = controllers.TimeConstants.prettyTime.format(new Date(registered))

  override def equals(obj: Any) = obj match {
    case t: Reward => t.id == id
    case _ => false
  }

  val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

  override def toString = id.toString

  def getFormattedAmount(pattern: String = "#.###"): String = {
    val df = new DecimalFormat(pattern)
    df.format(new BigDecimal(this.amount).divide(AppConstants.ETH_CAPACITY_BD))
  }

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

}

object Reward {

  def apply(
    amount: String,
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    registered: Long,
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String],
    owner: Option[Account],
  ) = new Reward(
    amount,
    id,
    lotteryAddress,
    lotteryIndex,
    ownerAddress,
    ownerId,
    registered,
    roomId,
    ticketNumber,
    txHash,
    owner,
  )

  def apply(
    amount: String,
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    registered: Long,
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
 ) = new Reward(
    amount,
    id,
    lotteryAddress,
    lotteryIndex,
    ownerAddress,
    ownerId,
    registered,
    roomId,
    ticketNumber,
    txHash,
    None,
  )

}
