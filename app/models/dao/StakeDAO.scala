package models.dao

import java.awt.print.Book

import scala.concurrent.Future
import javax.inject.Inject
import models.Stake
import models.StakeStatus.StakeStatus
import play.api.inject.ApplicationLifecycle

trait StakeDAO {

  def close: Future[Unit]

  def create(
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ): Future[Stake]

  def findById(id: Long): Future[Option[Stake]]

  def findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Stake]]

  def findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Stake]]

  def findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long): Future[Option[Stake]]

  def findByTxHash(txHash: String): Future[Option[Stake]]

  // stakes have rewards that are not paid yet (relewant Reward has no EthTx)
  def findUnpaid(roomId: Long): Future[Seq[Stake]]

  // stakes have no rewards
  def findUnprocessed(roomId: Long): Future[Seq[Stake]]

  def listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Stake]]

  def listPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

  def stakesByRoomLotteryIndex(RoomId: Long, lotteryIndex: Long, count: Int): Future[Seq[Stake]]

  def update(
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ): Future[Boolean]

  def update(id: Long, lotteryIndex: Option[Long]): Future[Boolean]

}

class StakeDAOCloseHook @Inject() (dao: StakeDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
