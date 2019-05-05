package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import controllers.AuthRequestToAppContext.ac
import models.Options
import models.dao.{DAOProvider, OptionDAO, RewardDAO, StakeDAO}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class AppController @Inject()(
	deadbolt: DeadboltActions,
	cc: ControllerComponents,
	config: Configuration,
	optionDAO: OptionDAO,
	rewardDAO: RewardDAO,
	stakeDAO: StakeDAO
)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends AbstractController(cc)
		with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def index = deadbolt.WithAuthRequest()() { implicit request =>
		for {
			lotteryAddress <- optionDAO.getOptionByName(Options.ETH_CONTRACT_ADDRESS).map(_.map(_.value))
			stakes <- stakeDAO.listPage(AppConstants.DEFAULT_PAGE_SIZE, 1, Seq.empty, None)
			rewards <- rewardDAO.listPage(AppConstants.DEFAULT_PAGE_SIZE, 1, Seq.empty, None)
		} yield Ok(views.html.app.index(lotteryAddress, stakes, rewards))
	}

	def latestRewards = deadbolt.WithAuthRequest()() { implicit request =>
		rewardDAO.listPage(AppConstants.DEFAULT_PAGE_SIZE, 1, Seq.empty, None) map { rewards =>
			Ok(views.html.app.common.latestRewards(rewards))
		}
	}

	def latestStakes = deadbolt.WithAuthRequest()() { implicit request =>
		stakeDAO.listPage(AppConstants.DEFAULT_PAGE_SIZE, 1, Seq.empty, None) map { stakes =>
			Ok(views.html.app.common.latestStakes(stakes))
		}
	}

	def panel = deadbolt.SubjectPresent()() { implicit request =>
		future(Ok(views.html.admin.panel()))
	}

}
