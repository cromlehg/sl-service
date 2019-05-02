package models

case class BOption(
  val id: Long,
  val name: String,
  val value: String,
  val ttype: String,
  val descr: String) {

  def toBoolean = value.toBoolean

  def toOptInt =
    if (value.trim.isEmpty)
      None
    else
      try Some(value.trim.toInt) catch {
        case _: Throwable => None
      }

  def toOptLong =
    if (value.trim.isEmpty)
      None
    else
      try Some(value.trim.toLong) catch {
        case _: Throwable => None
      }

}

object TOption {

  def apply(
    id: Long,
    name: String,
    value: String,
    ttype: String,
    descr: String): BOption =
    new BOption(
      id,
      name,
      value,
      ttype,
      descr)

}

object Options {

  val TYPE_BOOLEAN = "Boolean"

  val REGISTER_ALLOWED = "REGISTER_ALLOWED"

  val POSTS_CHANGE_ALLOWED = "POSTS_CHANGE_ALLOWED"

  val POSTS_CREATE_ALLOWED = "POSTS_CREATE_ALLOWED"

  val AFTER_PAGE_SCRIPT = "AFTER_PAGE_SCRIPT"

  val MAIN_MENU_ID = "MAIN_MENU_ID"

  val INDEX_PAGE_ID = "INDEX_PAGE_ID"

	val ETH_BLOCK_DELAY = "ETH_BLOCK_DELAY"

	val ETH_CHECKED_BLOCK_NUM = "ETH_CHECKED_BLOCK_NUM"

	val ETH_CONTRACT_ADDRESS = "ETH_CONTRACT_ADDRESS"

	val ETH_NODE_PROVIDER = "ETH_NODE_PROVIDER"

	val ETH_START_BLOCK_NUM = "ETH_START_BLOCK_NUM"

}
