package tasks

import akka.actor.ActorSystem
import javax.inject.Inject
import models._
import models.dao._
import play.Logger
import services._

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class FetchEventsTask @Inject()(
	accountDAO: AccountDAO,
	actorSystem: ActorSystem,
	optionDAO: OptionDAO,
	rewardDAO: RewardDAO,
	stakeDAO: StakeDAO,
	util: Util,
	web3jService: Web3jService,
)(implicit executionContext: ExecutionContext) {

	import scala.concurrent.Future.{successful => future}

	actorSystem.scheduler.schedule(initialDelay = 0.minutes, interval = 5.seconds) {
		val start = System.currentTimeMillis
		Logger.debug("FETCH EVENTS start")

		val result = optionDAO.getOptionByName(Options.ETH_CONTRACT_ADDRESS).map(_.map(_.value)) flatMap {
			case None => Future.failed(new Exception("Could not find ETH_CONTRACT_ADDRESS in the options list"))
			case Some(address) => optionDAO.getOptionByName(Options.ETH_BLOCK_DELAY).map(_.map(_.value.toLong)) flatMap {
				case None => Future.failed(new Exception("Could not find ETH_BLOCK_DELAY in the options list"))
				case Some(blockDelay) => optionDAO.getOptionByName(Options.ETH_START_BLOCK_NUM).map(_.map(_.value.toLong)) flatMap {
					case None => Future.failed(new Exception("Could not find ETH_START_BLOCK_NUM in the options list"))
					case Some(startBlockNum) => optionDAO.getOptionByName(Options.ETH_CHECKED_BLOCK_NUM).map(_.map(_.value.toLong)) flatMap { checkedBlockNum =>
						val fromBlock = checkedBlockNum.map(_ + 1).getOrElse(startBlockNum)
						val toBlock = web3jService.getBlockNumber - blockDelay
						if (fromBlock >= toBlock) {
							future(false)
						} else {
							val events = web3jService.getEvents(address, fromBlock, toBlock)
							val eventsInfo = events.flatten.map(e => (e.log.getTransactionHash, e.log.getLogIndex.toString))
							Logger.debug(s"${events.size} events found: $eventsInfo")
							util.serializeFutures(events) {
								case Some(event: TicketPurchasedEvent) => processTicketPurchase(event, address)
								case _ => future(false)
							} flatMap { processedEvents =>
								Logger.debug(s"Contract checked. ${processedEvents.count(e => e)} new events processed.")
								future(true)
							}
						}
					}
				}
			}
		}

		Await.ready(result, Duration.Inf).onComplete {
			case Success(_) => Logger.debug("FETCH EVENTS success")
			case Failure(e) => Logger.debug("FETCH EVENTS fail: " + e.getMessage + e.printStackTrace)
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// helper functions
	// -------------------------------------------------------------------------------------------------------------------

	def processTicketPurchase(event: TicketPurchasedEvent, address: String): Future[Boolean] =
		stakeDAO.create(
			address,
			Some(event.lotIndex.toLong),
			None,
			event.player,
			None,
			1,
			StakeStatus.ACCEPTED,
			event.ticketNumber.toLong,
			event.ticketPrice,
			Some(event.timestamp),
			Some(event.log.getTransactionHash),
		) map { _ => true }

}
