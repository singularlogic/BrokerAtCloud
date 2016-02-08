-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.5-10.0.16-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             8.3.0.4694
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table pulsar-feedback-db.consumer_feedback
CREATE TABLE IF NOT EXISTS `consumer_feedback` (
  `consumerId` varchar(4000) NOT NULL,
  `serviceId` varchar(4000) NOT NULL,
  `attributeId` varchar(4000) NOT NULL,
  `value` varchar(4000) NOT NULL,
  `lastChangedTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pulsar-feedback-db.consumer_feedback: ~35 rows (approximately)
DELETE FROM `consumer_feedback`;
/*!40000 ALTER TABLE `consumer_feedback` ENABLE KEYS */;


-- Dumping structure for table pulsar-feedback-db.feedback_notifications
CREATE TABLE IF NOT EXISTS `feedback_notifications` (
  `serviceId` varchar(4000) NOT NULL,
  `attributeId` varchar(4000) NOT NULL,
  `message` varchar(4000) NOT NULL,
  `creationTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pulsar-feedback-db.feedback_notifications: ~8 rows (approximately)
DELETE FROM `feedback_notifications`;
/*!40000 ALTER TABLE `feedback_notifications` ENABLE KEYS */;


-- Dumping structure for table pulsar-feedback-db.used_services
CREATE TABLE IF NOT EXISTS `used_services` (
  `consumerId` varchar(4000) NOT NULL,
  `serviceId` varchar(4000) NOT NULL,
  `lastUsedTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('IN-USE','NOT-USED') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pulsar-feedback-db.used_services: ~6 rows (approximately)
DELETE FROM `used_services`;
/*!40000 ALTER TABLE `used_services` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
