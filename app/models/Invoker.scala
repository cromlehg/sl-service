package models

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

import play.api.libs.json.Json
import play.api.libs.json.Writes

case class Invoker(
  val id: Long,
  val ownerId: Long,
  val address: String,
  val privateKey: String,
  val registered: Long,
  val owner: Option[Account]) {

  lazy val createdPrettyTime =
    controllers.TimeConstants.prettyTime.format(new Date(registered))

  override def equals(obj: Any) = obj match {
    case invoker: Invoker => invoker.address == address
    case _ => false
  }

  val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

}

object Invoker {

  implicit val invokersSelect2Writes = new Writes[Invoker] {
    def writes(target: Invoker) = Json.obj(
      "id" -> target.id,
      "text" -> target.address)
  }

  def apply(
    id: Long,
    ownerId: Long,
    address: String,
    privateKey: String,
    registered: Long,
    owner: Option[Account]): Invoker =
    new Invoker(
      id,
      ownerId,
      address,
      privateKey,
      registered,
      owner)

  def apply(
    id: Long,
    ownerId: Long,
    address: String,
    privateKey: String,
    registered: Long): Invoker =
    new Invoker(
      id,
      ownerId,
      address,
      privateKey,
      registered,
      None)

}
