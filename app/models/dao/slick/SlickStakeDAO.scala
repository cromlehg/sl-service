package models.dao.slick

import javax.inject.{ Inject, Singleton }
import models.dao.StakeDAO
import models.{ Stake, StakeStatus }
import models.dao.slick.table.StakeTable
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ ExecutionContext, Future }
import slick.ast.Ordering.Direction
import slick.ast.Ordering.Asc
import slick.ast.Ordering.Desc
import slick.sql.SqlAction

@Singleton
class SlickStakeDAO @Inject() (
  val accountDAO: SlickAccountDAO,
  val dbConfigProvider: DatabaseConfigProvider,
  val rewardDAO: SlickRewardDAO
)(implicit ec: ExecutionContext)
  extends StakeDAO
  with StakeTable
  with SlickCommonDAO {

  import dbConfig.profile.api._

  import scala.concurrent.Future.{ successful => future }

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  def _findById(id: Long) =
    queryById(id).result.headOption

  def _findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long) =
    table.filter(t => t.roomId === roomId && t.lotteryIndex === lotteryIndex)

  def _findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long) =
    _findByRoomIdAndLotteryIndex(roomId, lotteryIndex)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)

  def _findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long) =
    table.filter(t => t.roomId === roomId && t.ticketNumber === ticketNumber)

  def _findByTxHash(txHash: String) =
    table.filter(_.txHash.toUpperCase === txHash.toUpperCase).result.headOption

  def _findUnpaid(roomId: Long) =
    table
      .filter(_.roomId === roomId)
      .join(rewardDAO.table).on((s, r) => s.roomId === r.roomId && s.ticketNumber === r.ticketNumber)
      .filter(_._2.txHash.isEmpty)

  /*
  def _findUnprocessed(roomId: Long) = for {
    (stakes, rewards) <- table.filter(_.roomId === roomId).joinLeft(rewardDAO.table).on((s, r) => s.roomId === r.roomId && s.ticketNumber === r.ticketNumber) if rewards.isEmpty
  } yield stakes
  */

  def _findUnprocessed(roomId: Long) =
    table
      .filter(_.roomId === roomId)
      .joinLeft(rewardDAO.table).on((s, r) => s.roomId === r.roomId && s.ticketNumber === r.ticketNumber)
      .filter(_._2.isEmpty)
      .map(_._1)


  def _create(
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ) = table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += Stake(
    0,
    lotteryAddress,
    lotteryIndex,
    netAmount,
    ownerAddress,
    ownerId,
    System.currentTimeMillis,
    roomId,
    status,
    ticketNumber,
    ticketPrice,
    timestamp,
    txHash
  )

  def _listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    table
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)
  }

  def _listPageWithAccount(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    _listPage(pSize, pId, sortsBy, filterOpt)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)
  }

  def _listPagesCount(pSize: Int, filterOpt: Option[String]) = {
    table
      .size
  }

  def _update(
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ) = table
    .filter(_.id === id)
    .map(t => (t.lotteryAddress, t.lotteryIndex, t.netAmount, t.ownerAddress, t.ownerId, t.roomId, t.status, t.ticketNumber, t.ticketPrice, t.timestamp, t.txHash))
    .update(lotteryAddress, lotteryIndex, netAmount,ownerAddress, ownerId, roomId, status, ticketNumber, ticketPrice, timestamp, txHash)
    .map(_ == 1)

  def _update(
    id: Long,
    lotteryIndex: Option[Long]
  ) = table
    .filter(_.id === id)
    .map(t => t.lotteryIndex)
    .update(lotteryIndex)
    .map(_ == 1)

  def _stakesByRoomLotteryIndex(roomId: Long, lotteryIndex: Long, count: Int) =
    table
      .filter(_.roomId === roomId)
      .filter(_.lotteryIndex === lotteryIndex)
      .filter(_.status === models.StakeStatus.ACCEPTED)
      .dynamicSortBy(Seq(("id", Desc)))
      .distinctOn(_.ownerAddress)
      .take(count)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)

  override def listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Stake]] =
    db.run(_listPageWithAccount(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)
      .map { t =>
        t.map {
          case (stake, account) =>
            stake.copy(owner = account)
        }
      }

  override def stakesByRoomLotteryIndex(roomId: Long, lotteryIndex: Long, count: Int): Future[Seq[Stake]] =
    db.run(_stakesByRoomLotteryIndex(roomId, lotteryIndex, count).result).map { t =>
      t.map {
        case (stake, account) =>
          stake.copy(owner = account)
      }
    }

  override def listPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
    db.run(_listPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

  override def findById(id: Long): Future[Option[Stake]] =
    db.run(_findById(id))

  override def findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Stake]] =
    db.run(_findByRoomIdAndLotteryIndex(roomId, lotteryIndex).result)

  override def findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Stake]] =
    db.run(_findWithAccountByRoomIdAndLotteryIndex(roomId, lotteryIndex).result) map { _ map {
      case (stake, account) => stake.copy(owner = account)
    }}

  override def findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long): Future[Option[Stake]] =
    db.run(_findByRoomIdAndTicketNumber(roomId, ticketNumber).result.headOption)

  override def findByTxHash(txHash: String): Future[Option[Stake]] =
    db.run(_findByTxHash(txHash))

  override def findUnpaid(roomId: Long): Future[Seq[Stake]] =
    db.run(_findUnpaid(roomId).result).map(_.map(_._1))

  override def findUnprocessed(roomId: Long): Future[Seq[Stake]] =
    db.run(_findUnprocessed(roomId).result)

  override def create(
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ): Future[Stake] = db.run(_create(
    lotteryAddress,
    lotteryIndex,
    netAmount,
    ownerAddress,
    ownerId,
    roomId,
    status,
    ticketNumber,
    ticketPrice,
    timestamp,
    txHash
  ))

  override def close: Future[Unit] =
    future(db.close())

  override def update(
    id: Long,
    lotteryAddress: String,
    lotteryIndex: Option[Long],
    netAmount: Option[String],
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    status: StakeStatus.StakeStatus,
    ticketNumber: Long,
    ticketPrice: String,
    timestamp: Option[Long],
    txHash: Option[String]
  ): Future[Boolean] = db.run(_update(
    id,
    lotteryAddress,
    lotteryIndex,
    netAmount,
    ownerAddress,
    ownerId,
    roomId,
    status,
    ticketNumber,
    ticketPrice,
    timestamp,
    txHash
  ))

  override def update(id: Long, lotteryIndex: Option[Long]): Future[Boolean] =
    db.run(_update(id, lotteryIndex))

}
