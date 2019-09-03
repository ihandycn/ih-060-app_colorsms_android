package com.android.messaging.notificationcleaner;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class NotificationCleanerTest {
	// topic name : Notification Cleaner Test
	private static final String NOTIFICATION_CLEANER_TEST_TOPIC_ID = "topic-783o6bdil";

	public static boolean getSwitch() {
		return AutopilotConfig.getBooleanToTestNow(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"switch",false);
	}

	public static String getEmpty() {
		return AutopilotConfig.getStringToTestNow(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"empty","1");
	}

	public static void logNotificationAccessGrant() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"notification_access_grant");
	}

	public static void logNcHomepageShow() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"nc_homepage_show");
	}

	public static void logNcHomepageBtnClick() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"nc_homepage_btn_click");
	}

	public static void logNcHomepageAdShow() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"nc_homepage_ad_show");
	}

	public static void logResultpageShow() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"resultpage_show");
	}

	public static void logResultpageFulladShow() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"resultpage_fullad_show");
	}

	public static void logResultpageDoneadShow() {
		getSwitch();
		AutopilotEvent.logTopicEvent(NOTIFICATION_CLEANER_TEST_TOPIC_ID,"resultpage_donead_show");
	}
}
