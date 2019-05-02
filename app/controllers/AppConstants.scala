package controllers

import java.math.{BigInteger, BigDecimal}

object AppConstants {

  val APP_NAME = "sl-service"

  val VERSION = "0.1a"

  val BACKEND_NAME = APP_NAME + " " + VERSION

  val DEFAULT_PAGE_SIZE = 10

  val MAX_PAGE_SIZE = 100

  val SESSION_EXPIRE_TIME: Long = 3 * TimeConstants.DAY

  val PWD_MIN_LENGTH: Long = 12

	val SHORT_TITLE_DESCR = 50

  val DESCRIPTION_SIZE = 300

	val SHORT_DESCRIPTION_SIZE = 150

  val RETURN_URL = "referer"

	val HTTP_USER_AGENT = "User-Agent"

	val CHAT_MSGS_LIMIT = 5

	val ETH_CAPACITY = "1000000000000000000"

	val ETH_CAPACITY_BI = new BigInteger(ETH_CAPACITY)

	val ETH_CAPACITY_BD = new BigDecimal(ETH_CAPACITY)

	val ETH_ADDR_REGEXP = """0x[a-fA-F0-9]{40}"""

	val ETH_PRIVATE_KEY_REGEXP = """[a-fA-F0-9]{64}"""

	val ETH_VALUE_REGEXP = """(\d{0,18}\.\d{0,18})|(\d{0,18})"""

	val ETH_GAS_LIMIT = "300000"

	val ETH_GAS_PRICE = "7000000000"

}
