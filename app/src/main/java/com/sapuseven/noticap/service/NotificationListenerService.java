package com.sapuseven.noticap.service;

import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.sapuseven.noticap.utils.FilterRule;
import com.sapuseven.noticap.utils.SSHClient;
import com.sapuseven.noticap.utils.SSHIdentity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.DataFormatException;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {
	private final String TAG = this.getClass().getSimpleName();

	private static boolean isTimeBetween(String fromTime, String toTime, String nowTime) throws ParseException {
		String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9])$";
		if (fromTime.matches(reg) && toTime.matches(reg) && nowTime.matches(reg)) {
			boolean valid;

			Date startTime = new SimpleDateFormat("HH:mm", Locale.US).parse(fromTime);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(startTime);

			Date currentTime = new SimpleDateFormat("HH:mm", Locale.US).parse(nowTime);
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(currentTime);

			Date endTime = new SimpleDateFormat("HH:mm", Locale.US).parse(toTime);
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(endTime);

			if (currentTime.compareTo(endTime) < 0) {
				currentCalendar.add(Calendar.DATE, 1);
				currentTime = currentCalendar.getTime();
			}

			if (startTime.compareTo(endTime) < 0) {
				startCalendar.add(Calendar.DATE, 1);
				startTime = startCalendar.getTime();
			}

			if (currentTime.before(startTime)) {
				valid = false;
			} else {
				if (currentTime.after(endTime)) {
					endCalendar.add(Calendar.DATE, 1);
					endTime = endCalendar.getTime();
				}

				valid = currentTime.before(endTime);
			}
			return valid;
		} else {
			throw new IllegalArgumentException("Not a valid time, expecting HH:mm format");
		}
	}

	@Override
	public void onNotificationPosted(StatusBarNotification notification) {
		Log.i(TAG, "*** NOTIFICATION POSTED **********");
		Log.i(TAG, "* ID      : " + notification.getId());
		Log.i(TAG, "* TEXT    : " + notification.getNotification().tickerText);
		Log.i(TAG, "* PACKAGE : " + notification.getPackageName());
		Log.i(TAG, "**********************************");

		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug_master_toggle", true)) {
			Log.i(TAG, "* ACTIONS DISABLED IN PREFERENCES");
			Log.i(TAG, "**********************************");
			return;
		}

		try {
			JSONArray savedRules = FilterRule.loadSavedFilterRules(this, false).getJSONArray("rules");

			for (int i = 0; i < savedRules.length(); i++) {
				JSONObject ruleObj = savedRules.getJSONObject(i);
				if (ruleObj == null)
					continue;
				JSONArray packages = ruleObj.getJSONArray("packageNames");
				for (int j = 0; j < packages.length(); j++) {
					if (packages.getString(j).equals(notification.getPackageName())) {
						FilterRule rule = new FilterRule(ruleObj);
						String currentTime = new SimpleDateFormat("HH:mm", Locale.US).format(Calendar.getInstance().getTime());
						if (!rule.useDaytime() || isTimeBetween(rule.getFrom(), rule.getTo(), currentTime)) {
							SSHIdentity identity = SSHIdentity.fromID(this, rule.getIdentityID());
							new SSHClient.RemoteCommand(rule.getExec(), identity).execute();
						}
					}
				}
			}
		} catch (IOException | DataFormatException | JSONException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification notification) {
		Log.i(TAG, "*** NOTIFICATION REMOVED *********");
		Log.i(TAG, "* ID      : " + notification.getId());
		Log.i(TAG, "* TEXT    : " + notification.getNotification().tickerText);
		Log.i(TAG, "* PACKAGE : " + notification.getPackageName());
		Log.i(TAG, "**********************************");

		// TODO: Add actions for removed Notifications
	}
}
