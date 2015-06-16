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
/*!40000 ALTER TABLE `consumer_feedback` DISABLE KEYS */;
INSERT INTO `consumer_feedback` (`consumerId`, `serviceId`, `attributeId`, `value`, `lastChangedTimestamp`, `deleted`) VALUES
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AuditabilityPreferenceVariable', 'LOW', '2015-02-09 19:26:45', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'BAD', '2015-02-09 19:26:45', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-07 17:44:21', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'LOW', '2015-02-07 17:44:21', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-07 17:44:37', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'MEDIUM', '2015-02-07 17:44:37', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-07 19:17:11', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'MEDIUM', '2015-02-07 19:17:11', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-08 00:23:34', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'LOW', '2015-02-08 00:23:34', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-08 00:23:48', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-08 00:23:48', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'LOW', '2015-02-08 00:23:48', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-08 00:29:26', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'LOW', '2015-02-08 00:29:26', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AdaptabilityPreferenceVariable', 'HIGH', '2015-02-08 00:36:44', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'MEDIUM', '2015-02-08 00:36:44', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'BAD', '2015-02-09 20:56:53', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'HIGH', '2015-02-09 20:56:53', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '3;7;8', '2015-02-09 20:56:53', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'BAD', '2015-02-09 19:26:45', 0),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AuditabilityPreferenceVariable', 'LOW', '2015-02-09 19:26:45', 0),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '1001;1003;1005', '2015-02-09 19:26:45', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-09 19:33:58', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'HIGH', '2015-02-09 19:33:58', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '21;23;25', '2015-02-09 19:33:58', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-09 20:58:13', 1),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'MEDIUM', '2015-02-09 20:58:13', 1),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '13;16;19', '2015-02-09 20:58:13', 1),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-09 20:57:30', 0),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'HIGH', '2015-02-09 20:57:31', 0),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '0;0.1;0.2', '2015-02-09 20:57:31', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'GOOD', '2015-02-09 20:58:13', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'MEDIUM', '2015-02-09 20:58:13', 0),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', '0;1;1.1', '2015-02-09 20:58:13', 0);
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
/*!40000 ALTER TABLE `feedback_notifications` DISABLE KEYS */;
INSERT INTO `feedback_notifications` (`serviceId`, `attributeId`, `message`, `creationTimestamp`, `deleted`) VALUES
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', 'Attribute Minimum Response time: Service description value is lower than consumers\' perceivable value: sd-value=(0.060000, 0.100000, 0.200000), user-perception-value=(4.000000, 6.025000, 7.075000)', '2015-02-09 23:08:37', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'Attribute Learnability: Service description value is different than consumers\' perceivable value: sd-value=BAD, user-perception-value=MEDIUM', '2015-02-09 23:08:37', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'Attribute Accessibility: Service description value is different than consumers\' perceivable value: sd-value=LOW, user-perception-value=HIGH', '2015-02-09 23:08:37', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', 'Attribute Minimum Response time: Service description value is lower than consumers\' perceivable value: sd-value=(0.008000, 0.020000, 0.080000), user-perception-value=(511.000000, 513.000000, 515.000000)', '2015-02-09 23:08:37', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', 'Attribute \'Minimum Response time\': Service description value is lower than consumers\' perceivable value: sd-value=(0.060000, 0.100000, 0.200000), user-perception-value=(4.000000, 6.025000, 7.075000)', '2015-02-09 23:09:47', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#LearnabilityPreferenceVariable', 'Attribute \'Learnability\': Service description value is different than consumers\' perceivable value: sd-value=BAD, user-perception-value=MEDIUM', '2015-02-09 23:09:47', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#AccessibilityPreferenceVariable', 'Attribute \'Accessibility\': Service description value is different than consumers\' perceivable value: sd-value=LOW, user-perception-value=HIGH', '2015-02-09 23:09:47', 0),
	('http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', 'http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#MinimumPreferenceVariable', 'Attribute \'Minimum Response time\': Service description value is lower than consumers\' perceivable value: sd-value=(0.008000, 0.020000, 0.080000), user-perception-value=(511.000000, 513.000000, 515.000000)', '2015-02-09 23:09:47', 0);
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
/*!40000 ALTER TABLE `used_services` DISABLE KEYS */;
INSERT INTO `used_services` (`consumerId`, `serviceId`, `lastUsedTimestamp`, `status`) VALUES
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', '2015-02-02 16:41:03', 'IN-USE'),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp2', '2015-02-01 16:41:06', 'NOT-USED'),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp1', '2015-01-02 16:41:14', 'NOT-USED'),
	('admin', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', '2015-02-09 17:03:39', 'NOT-USED'),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASAddressApp1', '2015-02-09 19:27:00', 'IN-USE'),
	('sc1', 'http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#CASCalenderApp3', '2015-02-09 19:27:02', 'IN-USE');
/*!40000 ALTER TABLE `used_services` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
