package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.dao.RewardDAO
import models.dao.slick.table.RewardTable
import models.Reward
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickRewardDAO @Inject()(
  val accountDAO: SlickAccountDAO,
  val dbConfigProvider: DatabaseConfigProvider,
)(implicit ec: ExecutionContext)
  extends RewardDAO
  with RewardTable
  with SlickCommonDAO {

  import dbConfig.profile.api._

  import scala.concurrent.Future.{successful => future}

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  def _create(
    amount: String,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
  ) = table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += Reward(
    amount,
    0,
    lotteryAddress,
    lotteryIndex,
    ownerAddress,
    ownerId,
    System.currentTimeMillis,
    roomId,
    ticketNumber,
    txHash
  )

  def _findById(id: Long) =
    queryById(id).result.headOption

  def _findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long) =
    table.filter(t => t.roomId === roomId && t.lotteryIndex === lotteryIndex)

  def _findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long) =
    _findByRoomIdAndLotteryIndex(roomId, lotteryIndex)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)

  def _findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long) =
    table.filter(t => t.roomId === roomId && t.ticketNumber === ticketNumber)

  def _findLastWithAccountByRoomId(roomId: Long, count: Int, filter: Map[String, String]) =
    table
      .filter(_.roomId === roomId)
      .filterOpt(filter.get("nonzero")){ case (t, nonzero) => if (nonzero.toBoolean) t.amount =!= "0" else true.asColumnOf[Boolean] }
      .sortBy(_.id.desc)
      .take(count)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)

  def _listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    table
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)
  }

  def _listPagesCount(pSize: Int, filterOpt: Option[String]) = {
    table
      .size
  }

  def _listPageWithAccount(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    _listPage(pSize, pId, sortsBy, filterOpt)
      .joinLeft(accountDAO.table).on(_.ownerId === _.id)
  }

  def _update(
    id: Long,
    amount: String,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
  ) = table
    .filter(_.id === id)
    .map(t => (t.amount, t.lotteryAddress, t.lotteryIndex, t.ownerAddress, t.ownerId, t.roomId, t.ticketNumber, t.txHash))
    .update(amount, lotteryAddress, lotteryIndex, ownerAddress, ownerId, roomId, ticketNumber, txHash)
    .map(_ == 1)

  def _update(id: Long, txHash: Option[String]) =
    table
      .filter(_.id === id)
      .map(_.txHash)
      .update(txHash)
      .map(_ == 1)

  override def close: Future[Unit] =
    future(db.close())

  override def create(
    amount: String,
    lotteryAddress: String,
    lotteryIndex: Long,
    ownerAddress: String,
    ownerId: Option[Long],
    roomId: Long,
    ticketNumber: Long,
    txHash: Option[String]
  ): Future[Reward] = db.run(_create(
    amount,
    lotteryAddress,
    lotteryIndex,
    ownerAddress,
    ownerId,
    roomId,
    ticketNumber,
    txHash
  ))

  override def findById(id: Long): Future[Option[Reward]] =
    db.run(_findById(id))

  override def findByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Reward]] =
    db.run(_findByRoomIdAndLotteryIndex(roomId, lotteryIndex).result)

  override def findWithAccountByRoomIdAndLotteryIndex(roomId: Long, lotteryIndex: Long): Future[Seq[Reward]] =
    db.run(_findWithAccountByRoomIdAndLotteryIndex(roomId, lotteryIndex).result) map { _ map {
      case (reward, account) => reward.copy(owner = account)
    }}

  override def findByRoomIdAndTicketNumber(roomId: Long, ticketNumber: Long): Future[Option[Reward]] =
    db.run(_findByRoomIdAndTicketNumber(roomId, ticketNumber).result.headOption)

  override def findLastWithAccountByRoomId(roomId: Long, count: Int = -1, filter: Map[String, String]): Future[Seq[Reward]] =
    db.run(_findLastWithAccountByRoomId(roomId, count, filter).result) map { _ map {
      case (reward, account) => reward.copy(owner = account)
    }}

  override def listPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Reward]] =
    db.run(_listPageWithAccount(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)
      .map { t =>
        t.map {
          case (reward, account) => reward.copy(owner = account)
        }
      }

  override def listPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
    db.run(_listPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))


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
  ): Future[Boolean] = db.run(_update(
    id,
    amount,
    lotteryAddress,
    lotteryIndex,
    ownerAddress,
    ownerId,
    roomId,
    ticketNumber,
    txHash
  ))

  override def update(id: Long, txHash: Option[String]): Future[Boolean] =
    db.run(_update(id, txHash))

}
