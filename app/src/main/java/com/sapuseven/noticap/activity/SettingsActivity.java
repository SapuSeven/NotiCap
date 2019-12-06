package com.sapuseven.noticap.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sapuseven.noticap.BuildConfig;
import com.sapuseven.noticap.R;
import com.sapuseven.noticap.service.NotificationListenerService;
import com.sapuseven.noticap.utils.FilterRule;
import com.sapuseven.noticap.utils.SSHIdentity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

		startService(new Intent(this, NotificationListenerService.class));
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
		private final ArrayList<Preference> normalPreferences = new ArrayList<>();
		private final ArrayList<CheckBoxPreference> checkBoxPreferences = new ArrayList<>();
		private PreferenceCategory listCategory;
		private MenuItem actionButtonDelete;
		private MenuItem actionButtonConfirmDelete;
		private boolean deleteMode;
		private int selected;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_filters);
			setHasOptionsMenu(true);

			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);

			findPreference("setup").setOnPreferenceClickListener(preference -> {
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
				return true;
			});

			findPreference("add_filter_rule").setOnPreferenceClickListener(preference -> {
				Intent i = new Intent(getActivity(), AddFilterRuleActivity.class);
				startActivity(i);
				return true;
			});

			listCategory = (PreferenceCategory) findPreference("list_filter_rule");
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.toolbar_with_delete, menu);
			super.onCreateOptionsMenu(menu, inflater);

			actionButtonDelete = menu.findItem(R.id.action_delete);
			actionButtonConfirmDelete = menu.findItem(R.id.action_confirm_delete);
		}

		@Override
		public void onResume() {
			super.onResume();

			actionBar.setDisplayHomeAsUpEnabled(true);

			updatePrefs();
			showPrefs(false);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			switch (id) {
				case android.R.id.home:
					if (deleteMode)
						enableDeleteMode(false);
					else
						getActivity().finish();
					return true;
				case R.id.action_delete:
					enableDeleteMode(true);
					return true;
				case R.id.action_confirm_delete:
					deleteSelected();
					enableDeleteMode(false);
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}

		private void enableDeleteMode(boolean enable) {
			actionButtonDelete.setVisible(!enable);
			actionButtonConfirmDelete.setVisible(enable);

			listCategory.removeAll();

			if (enable) {
				showPrefs(true);
				updateSelectedItemCount();
			} else {
				updatePrefs();
				showPrefs(false);
				actionBar.setTitle(getString(R.string.pref_filter_rules));
			}

			deleteMode = enable;
		}

		private void deleteSelected() {
			try {
				JSONObject data = FilterRule.loadSavedFilterRules(getActivity(), false);
				for (int i = checkBoxPreferences.size() - 1; i >= 0; i--)
					if (checkBoxPreferences.get(i).isChecked())
						data.getJSONArray("rules").remove(i);
				FilterRule.saveRules(getActivity(), data);
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.error_write_file)
						.setMessage(getString(R.string.error_deleting_filter_rule, e.getMessage()))
						.setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
						.show();
			}
		}

		private void updatePrefs() {
			normalPreferences.clear();
			checkBoxPreferences.clear();

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

					Preference normalPref = new Preference(getActivity());
					normalPref.setTitle(rule.getName());
					final int index = i;
					normalPref.setOnPreferenceClickListener(preference -> {
						Intent intent = new Intent(getActivity(), AddFilterRuleActivity.class);
						intent.putExtra("modify", true);
						intent.putExtra("index", index);
						startActivity(intent);
						return true;
					});
					normalPreferences.add(normalPref);

					CheckBoxPreference checkBoxPref = new CheckBoxPreference(getActivity());
					checkBoxPref.setTitle(rule.getName());
					checkBoxPref.setOnPreferenceClickListener(preference -> {
						updateSelectedItemCount();
						return true;
					});
					checkBoxPreferences.add(checkBoxPref);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		private void showPrefs(boolean showCheckBoxes) {
			listCategory.removeAll();
			if (showCheckBoxes)
				for (int i = 0; i < checkBoxPreferences.size(); i++)
					listCategory.addPreference(checkBoxPreferences.get(i));
			else
				for (int i = 0; i < normalPreferences.size(); i++) {
					checkBoxPreferences.get(i).setChecked(false);
					listCategory.addPreference(normalPreferences.get(i));
				}
		}

		private void updateSelectedItemCount() {
			selected = 0;
			for (int i = 0; i < checkBoxPreferences.size(); i++)
				if (checkBoxPreferences.get(i).isChecked())
					selected++;
			actionBar.setTitle(getResources().getQuantityString(R.plurals.selected_item_count, selected, selected));
		}
	}

	public static class IdentitiesPreferenceFragment extends PreferenceFragment {
		private final ArrayList<Preference> normalPreferences = new ArrayList<>();
		private final ArrayList<CheckBoxPreference> checkBoxPreferences = new ArrayList<>();
		private PreferenceCategory listCategory;
		private MenuItem actionButtonDelete;
		private MenuItem actionButtonConfirmDelete;
		private boolean deleteMode;
		private int selected;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_identities);
			setHasOptionsMenu(true);

			findPreference("add_identity").setOnPreferenceClickListener(preference -> {
				Intent i = new Intent(getActivity(), AddSSHIdentityActivity.class);
				startActivity(i);
				return true;
			});

			listCategory = (PreferenceCategory) findPreference("list_identities");
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.toolbar_with_delete, menu);
			super.onCreateOptionsMenu(menu, inflater);

			actionButtonDelete = menu.findItem(R.id.action_delete);
			actionButtonConfirmDelete = menu.findItem(R.id.action_confirm_delete);
		}

		@Override
		public void onResume() {
			super.onResume();

			actionBar.setDisplayHomeAsUpEnabled(true);

			updatePrefs();
			showPrefs(false);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			switch (id) {
				case android.R.id.home:
					if (deleteMode)
						enableDeleteMode(false);
					else
						getActivity().finish();
					return true;
				case R.id.action_delete:
					enableDeleteMode(true);
					return true;
				case R.id.action_confirm_delete:
					deleteSelected();
					enableDeleteMode(false);
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}

		private void deleteSelected() {
			try {
				JSONObject data = SSHIdentity.loadSavedIdentities(getActivity(), false);
				for (int i = checkBoxPreferences.size() - 1; i >= 0; i--)
					if (checkBoxPreferences.get(i).isChecked())
						data.getJSONArray("identities").remove(i); // TODO: Check if the identity is being used by a filter rule
				SSHIdentity.saveIdentities(getActivity(), data);
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.error_write_file)
						.setMessage(getString(R.string.error_deleting_identity, e.getMessage()))
						.setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
						.show();
			}
		}

		private void enableDeleteMode(boolean enable) {
			actionButtonDelete.setVisible(!enable);
			actionButtonConfirmDelete.setVisible(enable);

			listCategory.removeAll();

			if (enable) {
				for (int i = 0; i < checkBoxPreferences.size(); i++) {
					listCategory.addPreference(checkBoxPreferences.get(i));
				}
				updateSelectedItemCount();
			} else {
				updatePrefs();
				for (int i = 0; i < normalPreferences.size(); i++) {
					checkBoxPreferences.get(i).setChecked(false);
					listCategory.addPreference(normalPreferences.get(i));
				}
				actionBar.setTitle(getString(R.string.pref_identities));
			}

			deleteMode = enable;
		}

		private void updatePrefs() {
			normalPreferences.clear();
			checkBoxPreferences.clear();

			JSONArray identities;
			try {
				identities = SSHIdentity.loadSavedIdentities(getActivity(), false).getJSONArray("identities");
			} catch (IOException | DataFormatException | JSONException e) {
				e.printStackTrace();
				identities = new JSONArray();
			}
			for (int i = 0; i < identities.length(); i++) {
				try {
					JSONObject identityObj = identities.getJSONObject(i);
					SSHIdentity identity = new SSHIdentity(identityObj);

					Preference normalPref = new Preference(getActivity());
					normalPref.setTitle(identity.getName());
					normalPref.setSummary(identity.getHost() + ":" + identity.getPort());
					final int index = i;
					normalPref.setOnPreferenceClickListener(preference -> {
						Intent intent = new Intent(getActivity(), AddSSHIdentityActivity.class);
						intent.putExtra("modify", true);
						intent.putExtra("index", index);
						startActivity(intent);
						return true;
					});
					normalPreferences.add(normalPref);
					listCategory.addPreference(normalPref);

					CheckBoxPreference checkBoxPref = new CheckBoxPreference(getActivity());
					checkBoxPref.setTitle(identity.getName());
					checkBoxPref.setSummary(identity.getHost() + ":" + identity.getPort());
					checkBoxPref.setOnPreferenceClickListener(preference -> {
						updateSelectedItemCount();
						return true;
					});
					checkBoxPreferences.add(checkBoxPref);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		private void showPrefs(boolean showCheckBoxes) {
			listCategory.removeAll();
			if (showCheckBoxes)
				for (int i = 0; i < checkBoxPreferences.size(); i++)
					listCategory.addPreference(checkBoxPreferences.get(i));
			else
				for (int i = 0; i < normalPreferences.size(); i++) {
					checkBoxPreferences.get(i).setChecked(false);
					listCategory.addPreference(normalPreferences.get(i));
				}
		}

		private void updateSelectedItemCount() {
			selected = 0;
			for (int i = 0; i < checkBoxPreferences.size(); i++)
				if (checkBoxPreferences.get(i).isChecked())
					selected++;
			actionBar.setTitle(getResources().getQuantityString(R.plurals.selected_item_count, selected, selected));
		}
	}

	public static class InfosPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_infos);
			setHasOptionsMenu(true);

			findPreference("version").setSummary(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

			PreferenceScreen prefScreen = getPreferenceScreen();
			for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
				Preference pref = prefScreen.getPreference(i);
				if (!BuildConfig.DEBUG && pref.getKey().startsWith("debug_"))
					prefScreen.removePreference(pref);
			}

			if (BuildConfig.DEBUG) {
				findPreference("debug_send_notification").setSummary("Package name: " + BuildConfig.APPLICATION_ID);
				findPreference("debug_send_notification").setOnPreferenceClickListener(preference -> {
					((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,
							new NotificationCompat.Builder(getActivity())
									.setSmallIcon(R.drawable.info)
									.setContentTitle("Test Notification")
									.setContentText("Use " + BuildConfig.APPLICATION_ID + " as the package name to test your actions")
									.build());
					return true;
				});
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				getActivity().finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
