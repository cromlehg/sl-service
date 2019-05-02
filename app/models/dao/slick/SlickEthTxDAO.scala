package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import models.{EthTx, EthTxActionType, EthTxReceiptStatus, EthTxStatus}
import models.dao.EthTxDAO
import models.dao.slick.table.EthTxTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.Asc
import slick.ast.Ordering.Desc
import slick.ast.Ordering.Direction

@Singleton
class SlickEthTxDAO @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val invokerDAO: SlickInvokerDAO
)(implicit ec: ExecutionContext)
  extends EthTxDAO with EthTxTable with SlickCommonDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  private val queryByHash = Compiled(
    (hash: Rep[String]) => table.filter(_.hash === hash))

  def _findById(id: Long) =
    queryById(id).result.headOption

  def _pendingTxs(invokerId: Long) =
    table
      .filter(tx => tx.invokerId === invokerId && tx.status === EthTxStatus.PENDING)
      .result

  def _unknownTxs(invokerId: Long) =
    table
      .filter(tx => tx.invokerId === invokerId && tx.status === EthTxStatus.UNKNOWN)
      .result

  def _findByHash(hash: String) =
    queryByHash(hash).result.headOption

  def _findByEthTxStatus(status: EthTxStatus.EthTxStatus) =
    table
      .filter(_.status === status)
      .result

  def _findByTargetId(id: Long) =
    table
      .filter(_.targetId === id)
      .result

  def _page(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], invokerId: Option[Long]) =
    table
      .filterOpt(invokerId) { case (t, filter) => t.invokerId === filter }
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)

  def _pageWithInvoker(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
    _page(pSize, pId, sortsBy, filterOpt, ownerId)
      .join(invokerDAO.table).on(_.invokerId === _.id)

  def _pagesCount(pSize: Int, filterOpt: Option[String], invokerId: Option[Long]) =
    table
      .filterOpt(invokerId) { case (t, filter) => t.invokerId === filter }
      .size

  def _create(
    actionType: EthTxActionType.EthTxActionType,
    blockNumber: Option[Long],
    contractAddress: Option[String],
    error: Option[String],
    from: String,
    gasLimit: Option[String],
    gasPrice: Option[String],
    gasUsed: Option[String],
    hash: Option[String],
    id: Long,
    invokerId: Option[Long],
    nonce: Option[String],
    registered: Long,
    receiptStatus: EthTxReceiptStatus.EthTxReceiptStatus,
    status: EthTxStatus.EthTxStatus,
    targetId: Option[Long],
    to: Option[String],
    transactionIndex: Option[String],
    value: String
  ) = table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += EthTx(
    actionType,
    blockNumber,
    contractAddress,
    error,
    from,
    gasLimit,
    gasPrice,
    gasUsed,
    hash,
    0,
    invokerId,
    nonce,
    System.currentTimeMillis,
    receiptStatus,
    status,
    targetId,
    to,
    transactionIndex,
    value
  )

  def _update(
    id: Long,
    blockNumber: Option[Long],
    contractAddress: Option[String],
    from: String,
    gasLimit: Option[String],
    gasPrice: Option[String],
    gasUsed: Option[String],
    hash: Option[String],
    nonce: Option[String],
    receiptStatus: EthTxReceiptStatus.EthTxReceiptStatus,
    status: EthTxStatus.EthTxStatus,
    to: Option[String],
    transactionIndex: Option[String],
    value: String
   ) = table
      .filter(_.id === id)
      .map(t => (t.blockNumber, t.contractAddress, t.from, t.gasLimit, t.gasPrice, t.gasUsed, t.hash, t.nonce, t.receiptStatus, t.status, t.to, t.transactionIndex, t.value))
      .update(blockNumber, contractAddress, from, gasLimit, gasPrice, gasUsed, hash, nonce, receiptStatus, status, to, transactionIndex, value)
      .map(_ == 1)

  def _update(id: Long, hash: Option[String], status: EthTxStatus.EthTxStatus, receiptStatus: EthTxReceiptStatus.EthTxReceiptStatus) = {
    table
      .filter(_.id === id)
      .map(t => (t.hash, t.status, t.receiptStatus))
      .update(hash, status, receiptStatus)
      .map(_ == 1)
  }
  override def create(
    actionType: EthTxActionType.EthTxActionType,
    blockNumber: Option[Long],
    contractAddress: Option[String],
    error: Option[String],
    from: String,
    gasLimit: Option[String],
    gasPrice: Option[String],
    gasUsed: Option[String],
    hash: Option[String],
    invokerId: Option[Long],
    nonce: Option[String],
    receiptStatus: EthTxReceiptStatus.EthTxReceiptStatus,
    status: EthTxStatus.EthTxStatus,
    targetId: Option[Long],
    to: Option[String],
    transactionIndex: Option[String],
    value: String,
  ): Future[EthTx] = db.run(_create(
    actionType,
    blockNumber,
    contractAddress,
    error,
    from,
    gasLimit,
    gasPrice,
    gasUsed,
    hash,
    0,
    invokerId,
    nonce,
    System.currentTimeMillis,
    receiptStatus,
    status,
    targetId,
    to,
    transactionIndex,
    value
  ).transactionally)

  override def create(
    actionType: EthTxActionType.EthTxActionType,
    error: Option[String],
    from: String,
    gasLimit: String,
    gasPrice: String,
    hash: Option[String],
    invokerId: Option[Long],
    nonce: String,
    status: EthTxStatus.EthTxStatus,
    targetId: Option[Long],
    to: String
  ): Future[EthTx] = db.run(_create(
    actionType,
    None,
    None,
    error,
    from,
    Some(gasLimit),
    Some(gasPrice),
    None,
    hash,
    0,
    invokerId,
    Some(nonce),
    System.currentTimeMillis,
    EthTxReceiptStatus.UNKNOWN,
    status,
    targetId,
    Some(to),
    None,
    "0"
  ).transactionally)

  override def create(
    actionType: EthTxActionType.EthTxActionType,
    from: String,
    gasLimit: String,
    gasPrice: String,
    hash: Option[String],
    invokerId: Option[Long],
    nonce: String,
    targetId: Option[Long],
    to: String
  ): Future[EthTx] = db.run(_create(
    actionType,
    None,
    None,
    None,
    from,
    Some(gasLimit),
    Some(gasPrice),
    None,
    hash,
    0,
    invokerId,
    Some(nonce),
    System.currentTimeMillis,
    EthTxReceiptStatus.UNKNOWN,
    EthTxStatus.UNKNOWN,
    targetId,
    Some(to),
    None,
    "0"
  ).transactionally)

  override def update(id:  Long, blockNumber: Option[Long], contractAddress: Option[String], from: String, gasLimit: Option[String], gasPrice: Option[String], gasUsed: Option[String], hash: Option[String], nonce: Option[String], receiptStatus: EthTxReceiptStatus.EthTxReceiptStatus, status: EthTxStatus.EthTxStatus, to: Option[String], transactionIndex: Option[String], value: String): Future[Boolean] =
    db.run(_update(id, blockNumber, contractAddress, from, gasLimit, gasPrice, gasUsed, hash, nonce, receiptStatus, status, to, transactionIndex, value).transactionally)

  override def update(id: Long, hash: Option[String], status: EthTxStatus.EthTxStatus, resultStatus: EthTxReceiptStatus.EthTxReceiptStatus): Future[Boolean] =
    db.run(_update(id, hash, status, resultStatus).transactionally)

  override def page(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], invokerId: Option[Long]): Future[Seq[EthTx]] =
    db.run(_pageWithInvoker(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, invokerId).result)
      .map(_.map { case (ethTx, invoker) => ethTx.copy(invoker = Some(invoker)) })

  override def pagesCount(pSize: Int, filterOpt: Option[String], invokerId: Option[Long]): Future[Int] =
    db.run(_pagesCount(pSize, filterOpt, invokerId).result).map(t => pages(t, pSize))

  override def findById(id: Long): Future[Option[EthTx]] =
    db.run(_findById(id))

  override def pendingTxs(invokerId: Long): Future[Seq[EthTx]] =
    db.run(_pendingTxs(invokerId))

  override def unknownTxs(invokerId: Long): Future[Seq[EthTx]] =
    db.run(_unknownTxs(invokerId))

  override def findByHash(hash: String): Future[Option[EthTx]] =
    db.run(_findByHash(hash))

  override def findByEthTxStatus(status: EthTxStatus.EthTxStatus): Future[Seq[EthTx]] =
    db.run(_findByEthTxStatus(status))

  override def findByTargetId(id: Long): Future[Seq[EthTx]] =
    db.run(_findByTargetId(id))

  override def close: Future[Unit] =
    future(db.close())

}
