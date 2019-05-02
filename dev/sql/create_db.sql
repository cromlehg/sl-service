CREATE DATABASE simplelottery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'simplelottery'@'localhost' IDENTIFIED BY 'simplelottery';
GRANT ALL PRIVILEGES ON simplelottery.* TO 'simplelottery'@'localhost';
FLUSH PRIVILEGES;
