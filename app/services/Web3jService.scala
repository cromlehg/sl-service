package services

import java.math.BigInteger

import javax.inject.{Inject, Singleton}
import javax.xml.bind.DatatypeConverter
import models.dao.OptionDAO
import models.{EthTxReceiptStatus, EthTxStatus}
import org.web3j.abi.datatypes.Type
import org.web3j.abi.{FunctionEncoder, FunctionReturnDecoder, TypeReference}
import org.web3j.crypto.{Credentials, Hash, RawTransaction, TransactionEncoder}
import org.web3j.protocol.core.methods.request.{EthFilter, Transaction}
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName, DefaultBlockParameterNumber}
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric

import scala.collection.JavaConverters._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

case class EventSignature(name: String, types: Seq[(String, String, Boolean)]) {
  override val toString: String = name + types.map(_._2).mkString("(", ",", ")")
  val sign: String = Hash.sha3(DatatypeConverter.printHexBinary(toString.getBytes("UTF-8")))
  def logMatch(log: Log): Boolean = log.getTopics.get(0) equals sign
}

case class Event(sign: EventSignature, log: Log, timestamp: Long) {
  val fields: Map[String, String] =
    (sign.types.filter(_._3) ++: sign.types.filterNot(_._3))
      .zip(log.getTopics.asScala.toSeq.tail ++: log.getData.substring(2).grouped(64).map("0x" + _).toSeq)
      .map {
        case ((name, "uint256", _), value) => (name, new BigInteger(value.substring(2), 16).toString)
        case ((name, "address", _), value) => (name, "0x" + value.substring(26).toLowerCase)
      }.toMap
  override val toString: String =
    sign.name + fields.map { case (name, value) => name + " = " + value }.mkString("(", ",", ")")
}

class TicketPurchasedEvent(override val log: Log, timestamp: Long) extends Event(TicketPurchasedEventSignature, log, timestamp) {
  val lotIndex = fields("lotIndex")
  val ticketNumber = fields("ticketNumber")
  val player = fields("player")
  val ticketPrice = fields("ticketPrice")
}

object TicketPurchasedEventSignature extends EventSignature(
  "TicketPurchased",
  Seq(
    ("lotIndex", "uint256", false),
    ("ticketNumber", "uint256", false),
    ("player", "address", false),
    ("ticketPrice", "uint256", false)
  )
)

case class TxExecutionResult (
  hash: Option[String],
  nonce: String,
  error: Option[String]
)

case class TxObject (
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
  value: String,
)

@Singleton
class Web3jService @Inject() (optionDAO: OptionDAO)(implicit executionContext: ExecutionContext) {

  import scala.concurrent.Future.{successful => future}

  lazy val web3: org.web3j.protocol.Web3j = {
    val r = optionDAO.getOptionByName(models.Options.ETH_NODE_PROVIDER) flatMap {
      case Some(option) => future(Some(option.value.trim))
      case _ => future(None)
    }
    val addr = Await.result(r, 5.seconds).get
    org.web3j.protocol.Web3j.build(new HttpService(addr))
  }

  private val millisToSeconds = (ms: Long) => new BigInteger(ms.toString).divide(new BigInteger("1000"))

  def getBlockNumber: Long = web3.ethBlockNumber.send.getBlockNumber.longValue

  def getBlockTimestamp(blockNumber: Long): Long = {
    val block = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(new BigInteger(blockNumber.toString)), false).send().getBlock
    block.getTimestamp.longValue * 1000
  }

  def getTxInfo(hash: String): Option[TxObject] = {
    var result: Option[TxObject] = None
    val ethTransaction = web3.ethGetTransactionByHash(hash).send
    if (ethTransaction == null) throw new Exception("Could not get response from EthTransaction")
    if (ethTransaction.getError != null) throw new Exception("Error while calling ethGetTransactionbyHash: " + ethTransaction.getError.getMessage)
    if (ethTransaction.getTransaction.isPresent) {
      val tx = ethTransaction.getTransaction.get
      val blockNumber = if (tx.getBlockNumberRaw != null) tx.getBlockNumber else null
      var txObject = TxObject(
        scala.Option(blockNumber).map(_.longValue),
        None,
        tx.getFrom,
        Some(tx.getGas.toString),
        Some(tx.getGasPrice.toString),
        None,
        Some(hash),
        Some(tx.getNonce.toString),
        EthTxReceiptStatus.UNKNOWN,
        EthTxStatus.PENDING,
        scala.Option(tx.getTo),
        scala.Option(tx.getTransactionIndex).map(_.toString),
        tx.getValue.toString
      )
      val ethGetTransactionReceipt = web3.ethGetTransactionReceipt(hash).send
      if (ethGetTransactionReceipt == null) throw new Exception("Could not get response from EthGetTransactionReceipt ")
      if (ethGetTransactionReceipt.getError != null) throw new Exception("Error while calling ethGetTransactionReceipt: " + ethGetTransactionReceipt.getError.getMessage)
      if (ethGetTransactionReceipt.getTransactionReceipt.isPresent) {
        val receipt = ethGetTransactionReceipt.getTransactionReceipt.get
        txObject = txObject.copy(
          blockNumber = Some(receipt.getBlockNumber).map(_.longValue),
          contractAddress = Some(receipt.getContractAddress),
          gasUsed = Some(receipt.getGasUsed).map(_.toString),
          status = EthTxStatus.MINED,
          receiptStatus = if (receipt.isStatusOK) EthTxReceiptStatus.SUCCESS else EthTxReceiptStatus.FAIL,
          transactionIndex = Some(receipt.getTransactionIndex).map(_.toString)
        )
      }
      result = Some(txObject)
    }
    result
  }

  def readSingleValue(contractAddress: String, valueName: String, valueType: String): String = {
    val typeRef = valueType match {
      case "address" => new TypeReference[org.web3j.abi.datatypes.Address]() {}
      case "bool" => new TypeReference[org.web3j.abi.datatypes.Bool]() {}
      case "uint256" => new TypeReference[org.web3j.abi.datatypes.Uint]() {}
    }
    val function = new org.web3j.abi.datatypes.Function(
      valueName,
      java.util.Arrays.asList[Type[_]](),
      java.util.Arrays.asList(typeRef)
    )
    val encodedFunction = FunctionEncoder.encode(function)
    val response = web3.ethCall(
      Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
      DefaultBlockParameterName.LATEST
    ).send
    val output = FunctionReturnDecoder.decode(response.getValue, function.getOutputParameters)
    val result = output.get(0).getValue.toString
    result
  }

  def write(
    contractAddress: String,
    publicKey: String,
    privateKey: String,
    gasPrice: String,
    gasLimit: String,
    function: org.web3j.abi.datatypes.Function
  ): Future[TxExecutionResult] = Future {
    val credentials = Credentials.create(privateKey)
    val nonce = web3.ethGetTransactionCount(publicKey, DefaultBlockParameterName.LATEST).send.getTransactionCount
    val encodedFunction = FunctionEncoder.encode(function)
    val transaction = RawTransaction.createTransaction(
      nonce,
      new BigInteger(gasPrice),
      new BigInteger(gasLimit),
      contractAddress,
      BigInteger.ZERO,
      encodedFunction
    )
    val signedMessage = TransactionEncoder.signMessage(transaction, credentials)
    val hexValue = Numeric.toHexString(signedMessage)
    val ethSendTransaction = web3.ethSendRawTransaction(hexValue).send
    if (ethSendTransaction == null) throw new Exception("Could not get response from EthSendTransaction")
    val hash = ethSendTransaction.getTransactionHash
    val errorMessage = ethSendTransaction.getError match {
      case null => None
      case error => Some(error.getMessage)
    }
    TxExecutionResult(Option(hash), nonce.toString, errorMessage)
  }

  // --------------------------------------------------------------------------
  // Events
  // --------------------------------------------------------------------------

  object EventFactory {
    def apply(log: Log): Option[Event] = {
      val timestamp = getBlockTimestamp(log.getBlockNumber.longValue)
      log.getTopics.get(0) match {
        case TicketPurchasedEventSignature.sign => Some(new TicketPurchasedEvent(log, timestamp))
        case _ => None
      }
    }
  }

  def getEvents(
    address: String,
    startBlockNum: Long,
    endBlockNum: Long
  ): Seq[Option[Event]] = {
    val ethFilter = new EthFilter(
      new DefaultBlockParameterNumber(startBlockNum),
      new DefaultBlockParameterNumber(endBlockNum),
      address)
    val ethLog = web3.ethGetLogs(ethFilter).send()
    val logs = ethLog.getLogs.asScala
    val events: Seq[Option[Event]] = logs map { log =>
      val logObject = log.get.asInstanceOf[Log]
      val event = EventFactory.apply(logObject)
      event
    }
    events
  }
}
