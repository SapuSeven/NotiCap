package com.sapuseven.noticap.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.sapuseven.noticap.R;
import com.sapuseven.noticap.utils.SSHIdentity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class AddSSHIdentityActivity extends AppCompatActivity {
	private EditText tvName;
	private EditText tvHost;
	private EditText tvPort;
	private EditText tvUsername;
	private EditText tvPassword;
	private EditText tvKeyfilePath;
	private EditText tvKeyfilePassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_sshidentity);
		tvName = findViewById(R.id.name);
		tvHost = findViewById(R.id.host);
		tvPort = findViewById(R.id.port);
		tvUsername = findViewById(R.id.username);
		tvPassword = findViewById(R.id.password);
		tvKeyfilePath = findViewById(R.id.keyfile_path);
		tvKeyfilePassword = findViewById(R.id.keyfile_password);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.add).setOnClickListener(view -> {
			try {
				saveIdentity();
			} catch (JSONException | IOException | DataFormatException e) {
				e.printStackTrace();
			}
		});

		findViewById(R.id.use_password).setOnClickListener(v -> {
			findViewById(R.id.password_container).setVisibility(View.VISIBLE);
			findViewById(R.id.keyfile_container).setVisibility(View.GONE);

			findViewById(R.id.use_password).setVisibility(View.GONE);
			findViewById(R.id.use_keyfile).setVisibility(View.VISIBLE);
			findViewById(R.id.use_password_activated).setVisibility(View.VISIBLE);
			findViewById(R.id.use_keyfile_activated).setVisibility(View.GONE);
		});

		final Activity activity = this;
		findViewById(R.id.use_keyfile).setOnClickListener(v -> {
			if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
			} else {
				findViewById(R.id.password_container).setVisibility(View.GONE);
				findViewById(R.id.keyfile_container).setVisibility(View.VISIBLE);

				findViewById(R.id.use_password).setVisibility(View.VISIBLE);
				findViewById(R.id.use_keyfile).setVisibility(View.GONE);
				findViewById(R.id.use_password_activated).setVisibility(View.GONE);
				findViewById(R.id.use_keyfile_activated).setVisibility(View.VISIBLE);
			}
		});

		if (getIntent().getBooleanExtra("modify", false)) {
			try {
				SSHIdentity identity = new SSHIdentity(SSHIdentity.loadSavedIdentities(this, false).getJSONArray("identities").getJSONObject(getIntent().getIntExtra("index", 0)));
				tvName.setText(identity.getName());
				tvHost.setText(identity.getHost());
				//noinspection AndroidLintSetTextI18n
				tvPort.setText(identity.getPort().toString());
				tvUsername.setText(identity.getUsername());
				tvPassword.setText(identity.getPassword());
				tvKeyfilePath.setText(identity.getKeyFilePath());
				tvKeyfilePassword.setText(identity.getKeyFilePassword());
				if (identity.getKeyFilePath() != null && identity.getKeyFilePath().length() > 0) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
					} else {
						findViewById(R.id.password_container).setVisibility(View.GONE);
						findViewById(R.id.keyfile_container).setVisibility(View.VISIBLE);

						findViewById(R.id.use_password).setVisibility(View.VISIBLE);
						findViewById(R.id.use_keyfile).setVisibility(View.GONE);
						findViewById(R.id.use_password_activated).setVisibility(View.GONE);
						findViewById(R.id.use_keyfile_activated).setVisibility(View.VISIBLE);
					}
				}
			} catch (IOException | DataFormatException | JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveIdentity() throws JSONException, IOException, DataFormatException {
		tvName.setError(null);
		tvHost.setError(null);
		tvPort.setError(null);
		tvUsername.setError(null);
		tvPassword.setError(null);
		tvKeyfilePath.setError(null);
		tvKeyfilePassword.setError(null);

		String name = tvName.getText().toString();
		String host = tvHost.getText().toString();
		String port = tvPort.getText().toString();
		String username = tvUsername.getText().toString();
		String password = tvPassword.getText().toString();
		String keyfilePath = tvKeyfilePath.getText().toString();
		String keyfilePassword = tvKeyfilePassword.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(name)) {
			tvName.setError(getString(R.string.error_field_required));
			focusView = tvName;
			cancel = true;
		} else if (TextUtils.isEmpty(host)) {
			tvHost.setError(getString(R.string.error_field_required));
			focusView = tvHost;
			cancel = true;
		} else if (TextUtils.isEmpty(port)) {
			tvPort.setError(getString(R.string.error_field_required));
			focusView = tvPort;
			cancel = true;
		} else if (port.replaceAll("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|665[0-2][0-9]|6553[0-5])$", "").length() > 0) {
			tvPort.setError(getString(R.string.invalid_port));
			focusView = tvPort;
			cancel = true;
		} else if (TextUtils.isEmpty(username)) {
			tvUsername.setError(getString(R.string.error_field_required));
			focusView = tvUsername;
			cancel = true;
		} else if (findViewById(R.id.keyfile_container).getVisibility() == View.VISIBLE && !(new File(keyfilePath)).exists()) {
			tvKeyfilePath.setError(getString(R.string.file_not_found));
			focusView = tvKeyfilePath;
			cancel = true;
		} else if (findViewById(R.id.keyfile_container).getVisibility() == View.VISIBLE && (new File(keyfilePath)).isDirectory()) {
			tvKeyfilePath.setError(getString(R.string.is_a_directory));
			focusView = tvKeyfilePath;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			final SSHIdentity identity = new SSHIdentity();
			identity.setName(name);
			identity.setHost(host);
			identity.setPort(Integer.parseInt(port));
			identity.setUsername(username);
			if (getIntent().getBooleanExtra("modify", false))
				identity.setID(SSHIdentity.loadSavedIdentities(this, false).getJSONArray("identities").getJSONObject(getIntent().getIntExtra("index", 0)).getLong("id"));
			else
				identity.setID(System.nanoTime());
			if (findViewById(R.id.keyfile_container).getVisibility() == View.VISIBLE) {
				identity.setKeyFilePath(keyfilePath);
				identity.setKeyFilePassword(keyfilePassword);
			} else {
				identity.setPassword(password);
			}
			if (addIdentity(identity, false)) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.identity_storage_corrupted_title)
						.setMessage(R.string.identity_storage_corrupted_body)
						.setPositiveButton(R.string.yes, (dialog, which) -> addIdentity(identity, true))
						.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
			} else {
				finish();
			}
		}
	}

	private boolean addIdentity(SSHIdentity identity, boolean overwrite) {
		try {
			JSONObject data = SSHIdentity.loadSavedIdentities(this, overwrite);
			if (getIntent().getBooleanExtra("modify", false))
				data.getJSONArray("identities").put(getIntent().getIntExtra("index", 0), identity.toJSONObject());
			else
				data.accumulate("identities", identity.toJSONObject());
			SSHIdentity.saveIdentities(this, data);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			if (overwrite)
				new AlertDialog.Builder(this)
						.setTitle(R.string.error_write_file)
						.setMessage(getString(R.string.error_saving_identity, e.getMessage()))
						.setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
						.show();
			return true;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case 1:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					findViewById(R.id.password_container).setVisibility(View.GONE);
					findViewById(R.id.keyfile_container).setVisibility(View.VISIBLE);

					findViewById(R.id.use_password).setVisibility(View.VISIBLE);
					findViewById(R.id.use_keyfile).setVisibility(View.GONE);
					findViewById(R.id.use_password_activated).setVisibility(View.GONE);
					findViewById(R.id.use_keyfile_activated).setVisibility(View.VISIBLE);
				} else {
					new AlertDialog.Builder(this)
							.setTitle(R.string.permission_required)
							.setMessage(R.string.permission_required_explaination)
							.setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
							.show();
				}
				break;
			default:
				break;
		}
	}
}