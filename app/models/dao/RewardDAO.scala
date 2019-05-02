package models.dao

import scala.concurrent.Future
import javax.inject.Inject
import models.Reward
import play.api.inject.ApplicationLifecycle

trait RewardDAO {

  def close: Future[Unit]

  def create(
    amount: String,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
  ): Future[Reward]

  def findById(id: Long): Future[Option[Reward]]

  def findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Reward]]

  def findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Reward]]

  def findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long): Future[Option[Reward]]

  def findLastWithAccountByRoomId(roomId: Long, count: Int = -1, filter: Map[String, String]): Future[Seq[Reward]]

  def listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Reward]]

  def listPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

  def update(
    id: Long,
    amount: String,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
  ): Future[Boolean]

  def update(
    id: Long,
    txHash: Option[String]
  ): Future[Boolean]

}

class RewardDAOCloseHook @Inject() (dao: RewardDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
