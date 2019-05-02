package models.dao.slick.table

import models.Reward

trait RewardTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[Reward](tag, "rewards")  with DynamicSortBySupport.ColumnSelector {
    def amount = column[String]("amount")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def lotteryAddress = column[String]("lottery_address")
    def lotteryIndex= column[Long]("lottery_index")
    def ownerAddress = column[String]("owner_address")
    def ownerId = column[Option[Long]]("owner_id")
    def registered = column[Long]("registered")
    def roomId = column[Long]("room_id")
    def ticketNumber = column[Long]("ticket_number")
    def txHash = column[Option[String]]("tx_hash")

    def * = (
      amount,
      id,
      lotteryAddress,
      lotteryIndex,
      ownerAddress,
      ownerId,
      registered,
      roomId,
      ticketNumber,
      txHash
    ) <> [Reward](
      t => Reward(
        t._1,
        t._2,
        t._3,
        t._4,
        t._5,
        t._6,
        t._7,
        t._8,
        t._9,
        t._10
      ), t => Some((
        t.amount,
        t.id,
        t.lotteryAddress,
        t.lotteryIndex,
        t.ownerAddress,
        t.ownerId,
        t.registered,
        t.roomId,
        t.ticketNumber,
        t.txHash
      ))
    )

    override val select = Map(
      "amount" -> this.amount,
      "id" -> this.id,
      "lotteryAddress" -> this.lotteryAddress,
      "lotteryIndex" -> this.lotteryIndex,
      "ownerAddress" -> this.ownerAddress,
      "ownerId" -> this.ownerId,
      "registered" -> this.registered,
      "roomId" -> this.roomId,
      "ticketNumber" -> this.ticketNumber,
      "txHash" -> this.txHash
    )

  }

  val table = TableQuery[InnerCommonTable]

}

