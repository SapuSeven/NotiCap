package com.sapuseven.noticap.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.sapuseven.noticap.BuildConfig;
import com.sapuseven.noticap.R;
import com.sapuseven.noticap.service.NotificationListenerService4;
import com.sapuseven.noticap.utils.FilterRule;
import com.sapuseven.noticap.utils.SSHIdentity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public class SettingsActivity extends AppCompatPreferenceActivity {
	private static ActionBar actionBar;

	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();

		startService(new Intent(this, NotificationListenerService4.class));
	}

	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| FiltersPreferenceFragment.class.getName().equals(fragmentName)
				|| IdentitiesPreferenceFragment.class.getName().equals(fragmentName)
				|| InfosPreferenceFragment.class.getName().equals(fragmentName);
	}

	public static class FiltersPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_filters);
			setHasOptionsMenu(true);

			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);

			findPreference("setup").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
					return true;
				}
			});

			findPreference("add_filter_rule").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), AddFilterRuleActivity.class);
					startActivity(i);
					return true;
				}
			});
		}

		@Override
		public void onResume() {
			super.onResume();
			PreferenceCategory listCategory = (PreferenceCategory) findPreference("list_filter_rule");
			listCategory.removeAll();
			JSONArray rules;
			try {
				rules = FilterRule.loadSavedFilterRules(getActivity(), false).getJSONArray("rules");
			} catch (IOException | DataFormatException | JSONException e) {
				e.printStackTrace();
				rules = new JSONArray();
			}
			for (int i = 0; i < rules.length(); i++) {
				try {
					JSONObject identityObj = rules.getJSONObject(i);
					FilterRule rule = new FilterRule(identityObj);
					Preference itemPref = new Preference(getActivity());
					itemPref.setTitle(rule.getName());
					final int index = i;
					itemPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Intent intent = new Intent(getActivity(), AddFilterRuleActivity.class);
							intent.putExtra("modify", true);
							intent.putExtra("index", index);
							startActivity(intent);
							return true;
						}
					});
					// TODO: Add some way to delete the filter rule
					listCategory.addPreference(itemPref);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	public static class IdentitiesPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_identities);
			setHasOptionsMenu(true);

			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);

			findPreference("add_identity").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), AddSSHIdentityActivity.class);
					startActivity(i);
					return true;
				}
			});
		}

		@Override
		public void onResume() {
			super.onResume();
			PreferenceCategory listCategory = (PreferenceCategory) findPreference("list_identities");
			listCategory.removeAll();
			JSONArray identities;
			try {
				identities = SSHIdentity.loadSavedIdentities(getActivity(), false)
						.getJSONArray("identities");
			} catch (IOException | DataFormatException | JSONException e) {
				e.printStackTrace();
				identities = new JSONArray();
			}
			for (int i = 0; i < identities.length(); i++) {
				JSONObject identityObj = identities.optJSONObject(i);
				if (identityObj == null)
					continue;
				SSHIdentity identity = new SSHIdentity(identityObj);
				Preference itemPref = new Preference(getActivity());
				itemPref.setTitle(identity.getName());
				itemPref.setSummary(identity.getHost() + ":" + identity.getPort());
				final int index = i;
				itemPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(getActivity(), AddSSHIdentityActivity.class);
						intent.putExtra("modify", true);
						intent.putExtra("index", index);
						startActivity(intent);
						return true;
					}
				});
				// TODO: Add some way to delete the identity
				listCategory.addPreference(itemPref);
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	public static class InfosPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_infos);
			setHasOptionsMenu(true);

			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);

			findPreference("version").setSummary(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

			PreferenceScreen prefScreen = getPreferenceScreen();
			for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
				Preference pref = prefScreen.getPreference(i);
				if (!BuildConfig.DEBUG && pref.getKey().startsWith("debug_"))
					prefScreen.removePreference(pref);
			}

			if (BuildConfig.DEBUG) {
				findPreference("debug_send_notification").setSummary("Package name: " + BuildConfig.APPLICATION_ID);
				findPreference("debug_send_notification").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,
								new NotificationCompat.Builder(getActivity())
										.setSmallIcon(R.drawable.info)
										.setContentTitle("Test Notification")
										.setContentText("Use " + BuildConfig.APPLICATION_ID + " as the package name to test your actions")
										.build());
						return true;
					}
				});
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
