package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Invoker
import play.api.inject.ApplicationLifecycle

trait InvokerDAO {

  def findById(id: Long): Future[Option[Invoker]]

  def findByAddress(login: String): Future[Option[Invoker]]

  def create(ownerId: Long, address: String, privateKey: String): Future[Invoker]

  def update(id: Long, address: String, privateKey: String): Future[Boolean]

  def page(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], ownerId: Option[Long]): Future[Seq[Invoker]]

  def pagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long]): Future[Int]

  def close: Future[Unit]

}

class InvokerDAOCloseHook @Inject() (dao: InvokerDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}