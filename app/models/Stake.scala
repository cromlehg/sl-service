package models

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Date

import controllers.AppConstants
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

case class Stake(
  id: Long,
  lotteryAddress: String,
  lotteryIndex: Option[Long],
  netAmount: Option[String],
  ownerAddress: String,
  ownerId: Option[Long],
  registered: Long,
  roomId: Long,
  status: StakeStatus.StakeStatus,
  ticketNumber: Long,
  ticketPrice: String,
  timestamp: Option[Long],
  txHash: Option[String],
  owner: Option[Account]
) {

  lazy val createdPrettyTime =
    controllers.TimeConstants.prettyTime.format(new Date(registered))

  override def equals(obj: Any) = obj match {
    case t: Stake => t.id == id
    case _ => false
  }

  val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

  override def toString = id.toString

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

  def getFormattedAmount(pattern: String = "#.###"): String = {
    val df = new DecimalFormat(pattern)
    df.format(new BigDecimal(this.ticketPrice).divide(AppConstants.ETH_CAPACITY_BD))
  }

}

object StakeStatus extends Enumeration() {
  type StakeStatus = Value
  val ACCEPTED = Value("accepted")
  val CREATED = Value("created")
  val REJECTED = Value("rejected")
}

object Stake {

  def apply(
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    registered: Long,
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String],
    owner: Option[Account]
  ) = new Stake(
    id,
    lotteryAddress,
    lotteryIndex,
    netAmount,
    ownerAddress,
    ownerId,
    registered,
    roomId,
    status,
    ticketNumber,
    ticketPrice,
    timestamp,
    txHash,
    owner
  )
  def apply(
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    registered: Long,
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String],
  ) = new Stake(
    id,
    lotteryAddress,
    lotteryIndex,
    netAmount,
    ownerAddress,
    ownerId,
    registered,
    roomId,
    status,
    ticketNumber,
    ticketPrice,
    timestamp,
    txHash,
    None
  )

}


