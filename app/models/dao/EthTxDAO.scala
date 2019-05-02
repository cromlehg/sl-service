package models.dao

import scala.concurrent.Future
import javax.inject.Inject
import models.{EthTx, EthTxActionType, EthTxReceiptStatus, EthTxStatus}
import play.api.inject.ApplicationLifecycle

trait EthTxDAO {

  def findById(id: Long): Future[Option[EthTx]]

  def findByHash(hash: String): Future[Option[EthTx]]

  def findByEthTxStatus(status: EthTxStatus.EthTxStatus): Future[Seq[EthTx]]

  def findByTargetId(id: Long): Future[Seq[EthTx]]

  def create(
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
  ): Future[EthTx]

  def create(
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
  ): Future[EthTx]

  def create(
    actionType: EthTxActionType.EthTxActionType,
    from: String,
    gasLimit: String,
    gasPrice: String,
    hash: Option[String],
    invokerId: Option[Long],
    nonce: String,
    targetId: Option[Long],
    to: String
  ): Future[EthTx]

  def update(
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
  ): Future[Boolean]

  def update(
    id: Long,
    hash: Option[String],
    status: EthTxStatus.EthTxStatus,
    resultStatus: EthTxReceiptStatus.EthTxReceiptStatus
  ): Future[Boolean]

  def page(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], invokerId: Option[Long]): Future[Seq[EthTx]]

  def pagesCount(pSize: Int, filterOpt: Option[String], invokerId: Option[Long]): Future[Int]

  def pendingTxs(invokerId: Long): Future[Seq[EthTx]]

  def unknownTxs(invokerId: Long): Future[Seq[EthTx]]

  def close: Future[Unit]

}

class EthTxDAOCloseHook @Inject() (dao: EthTxDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
