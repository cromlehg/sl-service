package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.Invoker
import models.dao.InvokerDAO
import models.dao.slick.table.InvokerTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.Asc
import slick.ast.Ordering.Desc
import slick.ast.Ordering.Direction

@Singleton
class SlickInvokerDAO @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val accountDAO: SlickAccountDAO)(implicit ec: ExecutionContext)
  extends InvokerDAO with InvokerTable with SlickCommonDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  private val queryByAddress = Compiled(
    (address: Rep[String]) => table.filter(_.address === address))

  def _findById(id: Long) =
    queryById(id).result.headOption

  def _findByAddress(address: String) =
    queryByAddress(address).result.headOption

  def _page(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
    table
      .filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
      .filterOpt(filterOpt) { case (t, filter) => t.address.like("%" + filter.trim + "%") || t.privateKey.like("%" + filter.trim + "%") }
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)

  def _pageWithOwner(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
    _page(pSize, pId, sortsBy, filterOpt, ownerId)
      .join(accountDAO.table).on(_.ownerId === _.id)

  def _pagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long]) =
    table
      .filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
      .filterOpt(filterOpt) { case (t, filter) => t.address.like("%" + filter.trim + "%") || t.privateKey.like("%" + filter.trim + "%") }
      .size

  def _create(ownerId: Long, address: String, privateKey: String) = {
    table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += models.Invoker(
      0,
      ownerId,
      address,
      privateKey,
      System.currentTimeMillis)
  }

  def _update(id: Long, address: String, privateKey: String) =
    table
      .filter(_.id === id)
      .map(t => (t.address, t.privateKey))
      .update(address, privateKey)
      .map(_ == 1)

  override def update(id: Long, address: String, privateKey: String): Future[Boolean] =
    db.run(_update(id, address, privateKey).transactionally)

  override def create(ownerId: Long, address: String, privateKey: String): Future[Invoker] =
    db.run(_create(ownerId, address, privateKey).transactionally)

  override def page(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], ownerId: Option[Long]): Future[Seq[Invoker]] =
    db.run(_pageWithOwner(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, ownerId).result)
      .map(_.map { case (invoker, account) => invoker.copy(owner = Some(account)) })

  override def pagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long]): Future[Int] =
    db.run(_pagesCount(pSize, filterOpt, ownerId).result).map(t => pages(t, pSize))

  override def findById(id: Long): Future[Option[Invoker]] =
    db.run(_findById(id))

  override def findByAddress(address: String): Future[Option[Invoker]] =
    db.run(_findByAddress(address))

  override def close: Future[Unit] =
    future(db.close())

}
