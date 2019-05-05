CREATE TABLE roles (
  id                       SERIAL PRIMARY KEY,
  `name`                   VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions (
  id                       SERIAL PRIMARY KEY,
  value                    VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE roles_to_targets (
  role_id                  BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account") NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions_to_targets (
  permission_id            BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account", "role")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (permission_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE tags_to_targets (
  tag_id                   BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("post")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (tag_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL,
  user_agent                TINYTEXT,
  os                        TINYTEXT,
  device                    TINYTEXT,
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       ENUM('confirmed', 'wait confirmation') NOT NULL,
  account_status            ENUM('normal', 'locked') NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100),
  password_recovery_code    VARCHAR(100),
  password_recovery_date    BIGINT UNSIGNED
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   TEXT NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE eth_txs (
	id                        SERIAL PRIMARY KEY,
	action_type               ENUM('create', 'finish', 'reward', 'unknown', 'update') NOT NULL,
	block_number              BIGINT UNSIGNED,
	contract_address          CHAR(42),
	error                     TEXT,
	`from`                    CHAR(42) NOT NULL,
	gas_limit                 VARCHAR(255),
	gas_price                 VARCHAR(255),
	gas_used                  VARCHAR(255),
	hash                      CHAR(66),
	invoker_id                BIGINT UNSIGNED,
	nonce                     VARCHAR(255),
	registered                BIGINT UNSIGNED NOT NULL,
	receipt_status            ENUM('fail', 'success', 'unknown') NOT NULL,
	status                    ENUM('fail', 'mined', 'pending', 'unknown') NOT NULL,
	target_id                 BIGINT UNSIGNED,
	`to`                      CHAR(42),
	transaction_index         VARCHAR(255),
	`value`                   VARCHAR(255) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE invokers (
	id                        SERIAL PRIMARY KEY,
	owner_id                  BIGINT UNSIGNED NOT NULL,
	address                   CHAR(42) UNIQUE NOT NULL,
	private_key               CHAR(64) UNIQUE NOT NULL,
	registered                BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE rewards (
	id                        SERIAL PRIMARY KEY,
	amount                    VARCHAR(255) NOT NULL,
	lottery_address           VARCHAR(42) NOT NULL,
	lottery_index             BIGINT UNSIGNED NOT NULL,
	owner_address             VARCHAR(42) NOT NULL,
	owner_id                  BIGINT UNSIGNED,
	registered                BIGINT UNSIGNED NOT NULL,
	room_id                   BIGINT UNSIGNED NOT NULL,
	ticket_number             BIGINT UNSIGNED NOT NULL,
	tx_hash                   CHAR(66)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE stakes (
	id                        SERIAL PRIMARY KEY,
	lottery_address           VARCHAR(42) NOT NULL,
	lottery_index             BIGINT UNSIGNED,
	net_amount                VARCHAR(255),
	owner_address             VARCHAR(42) NOT NULL,
	owner_id                  BIGINT UNSIGNED,
	registered                BIGINT UNSIGNED NOT NULL,
	room_id                   BIGINT UNSIGNED NOT NULL,
	status                    ENUM('accepted', 'created', 'rejected'),
	ticket_number             BIGINT UNSIGNED NOT NULL,
	ticket_price              VARCHAR(255) NOT NULL,
	timestamp                 BIGINT UNSIGNED,
	tx_hash                   CHAR(66)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
