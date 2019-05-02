package models

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

case class EthTx(
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
  value: String,

  invoker: Option[Invoker]
) {

  lazy val createdPrettyTime =
    controllers.TimeConstants.prettyTime.format(new Date(registered))

  override def equals(obj: Any) = obj match {
    case t: EthTx => t.hash == hash
    case _ => false
  }

  val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

}

object EthTxStatus extends Enumeration() {
  type EthTxStatus = Value
  val FAIL = Value("fail")
  val MINED = Value("mined")
  val PENDING = Value("pending")
  val UNKNOWN = Value("unknown")
}

object EthTxReceiptStatus extends Enumeration() {
  type EthTxReceiptStatus = Value
  val FAIL = Value("fail")
  val SUCCESS = Value("success")
  val UNKNOWN = Value("unknown")
}

object EthTxActionType extends Enumeration() {
  type EthTxActionType = Value
  val CREATE = Value("create")
  val FINISH = Value("finish")
  val REWARD = Value("reward")
  val UNKNOWN = Value("unknown")
  val UPDATE = Value("update")
}

object EthTx {

  def apply(
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
    value: String,
    invoker: Option[Invoker]
  ): EthTx = new EthTx(
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
    value,
    invoker
  )

  def apply(
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
  ): EthTx = new EthTx(
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
    value,
    None
  )

}
