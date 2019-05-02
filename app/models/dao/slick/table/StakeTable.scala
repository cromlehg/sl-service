package models.dao.slick.table

import models.Stake
import models.StakeStatus

trait StakeTable extends CommonTable {

  import dbConfig.profile.api._

  implicit val StakeStatusMapper = enum2String(StakeStatus)

  class InnerCommonTable(tag: Tag) extends Table[Stake](tag, "stakes")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def lotteryAddress = column[String]("lottery_address")
    def lotteryIndex= column[Option[Long]]("lottery_index")
    def netAmount = column[Option[String]]("net_amount")
    def ownerAddress = column[String]("owner_address")
    def ownerId = column[Option[Long]]("owner_id")
    def registered = column[Long]("registered")
    def roomId = column[Long]("room_id")
    def status = column[StakeStatus.StakeStatus]("status")
    def ticketNumber = column[Long]("ticket_number")
    def ticketPrice = column[String]("ticket_price")
    def timestamp = column[Option[Long]]("timestamp")
    def txHash = column[Option[String]]("tx_hash")

    def * = (
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
      txHash
    ) <> [Stake](
      t => Stake(
        t._1,
        t._2,
        t._3,
        t._4,
        t._5,
        t._6,
        t._7,
        t._8,
        t._9,
        t._10,
        t._11,
        t._12,
        t._13
      ), t => Some((
        t.id,
        t.lotteryAddress,
        t.lotteryIndex,
        t.netAmount,
        t.ownerAddress,
        t.ownerId,
        t.registered,
        t.roomId,
        t.status,
        t.ticketNumber,
        t.ticketPrice,
        t.timestamp,
        t.txHash
      ))
    )

    override val select = Map(
      "id" -> this.id,
      "lotteryAddress" -> this.lotteryAddress,
      "lotteryIndex" -> this.lotteryIndex,
      "netAmount" -> this.netAmount,
      "ownerAddress" -> this.ownerAddress,
      "ownerId" -> this.ownerId,
      "registered" -> this.registered,
      "roomId" -> this.roomId,
      "status" -> this.status,
      "ticketNumber" -> this.ticketNumber,
      "ticketPrice" -> this.ticketPrice,
      "timestamp" -> this.timestamp,
      "txHash" -> this.txHash
    )

  }

  val table = TableQuery[InnerCommonTable]

}

