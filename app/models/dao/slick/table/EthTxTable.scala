package models.dao.slick.table

import models.{EthTx, EthTxActionType, EthTxReceiptStatus, EthTxStatus}

trait EthTxTable extends CommonTable {

  import dbConfig.profile.api._

  implicit val EthTxStatusMapper = enum2String(EthTxStatus)

  implicit val EthTxReeiptStatusMapper = enum2String(EthTxReceiptStatus)

  implicit val ethTxActionTypeMapper = enum2String(EthTxActionType)

  class InnerCommonTable(tag: Tag) extends Table[models.EthTx](tag, "eth_txs")  with DynamicSortBySupport.ColumnSelector {
    def actionType = column[EthTxActionType.EthTxActionType]("action_type")
    def blockNumber = column[Option[Long]]("block_number")
    def contractAddress = column[Option[String]]("contract_address")
    def error = column[Option[String]]("error")
    def from = column[String]("from")
    def gasLimit = column[Option[String]]("gas_limit")
    def gasPrice = column[Option[String]]("gas_price")
    def gasUsed = column[Option[String]]("gas_used")
    def hash = column[Option[String]]("hash")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def invokerId = column[Option[Long]]("invoker_id")
    def nonce = column[Option[String]]("nonce")
    def registered = column[Long]("registered")
    def receiptStatus = column[EthTxReceiptStatus.EthTxReceiptStatus]("receipt_status")
    def status = column[EthTxStatus.EthTxStatus]("status")
    def targetId = column[Option[Long]]("target_id")
    def to = column[Option[String]]("to")
    def transactionIndex = column[Option[String]]("transaction_index")
    def value = column[String]("value")

    def * = (
      actionType,
      blockNumber,
      contractAddress,
      error,
      from,
      gasLimit,
      gasPrice,
      gasUsed,
      hash,
      id,
      invokerId,
      nonce,
      registered,
      receiptStatus,
      status,
      targetId,
      to,
      transactionIndex,
      value
    ) <> [EthTx](
      t => EthTx(
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
        t._13,
        t._14,
        t._15,
        t._16,
        t._17,
        t._18,
        t._19
      ), t => Some((
        t.actionType,
        t.blockNumber,
        t.contractAddress,
        t.error,
        t.from,
        t.gasLimit,
        t.gasPrice,
        t.gasUsed,
        t.hash,
        t.id,
        t.invokerId,
        t.nonce,
        t.registered,
        t.receiptStatus,
        t.status,
        t.targetId,
        t.to,
        t.transactionIndex,
        t.value,
      ))
    )

    override val select = Map(
      "actionType" -> this.actionType,
      "blockNumber" -> this.blockNumber,
      "contractAddress" -> this.contractAddress,
      "error" -> this.error,
      "from" -> this.from,
      "gasLimit" -> this.gasLimit,
      "gasPrice" -> this.gasPrice,
      "gasUsed" -> this.gasUsed,
      "hash" -> this.hash,
      "id" -> this.id,
      "invokerId" -> this.invokerId,
      "nonce" -> this.nonce,
      "registered" -> this.registered,
      "receiptStatus" -> this.receiptStatus,
      "status" -> this.status,
      "targetId" -> this.targetId,
      "to" -> this.to,
      "transactionIndex" -> this.transactionIndex,
      "value" -> this.value
    )

  }

  val table = TableQuery[InnerCommonTable]

}
