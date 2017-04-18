package com.sapuseven.noticap.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * @author paul
 * @version 1.0
 * @since 2017-04-05
 */

public class SSHIdentity {
	@NonNull
	private String name = "";
	@NonNull
	private String host = "";
	@NonNull
	private String username = "";
	private String password;
	private String keyFilePath;
	private String keyFilePassword;
	@NonNull
	private Integer port = 22;
	@NonNull
	private Long id = 0L;

	public SSHIdentity() {
	}

	private SSHIdentity(@NonNull String name, @NonNull String host, @NonNull String username, String password, String keyFilePath, String keyFilePassword, @NonNull Integer port, @NonNull Long id) {
		this.name = name;
		this.host = host;
		this.username = username;
		this.password = password;
		this.keyFilePath = keyFilePath;
		this.keyFilePassword = keyFilePassword;
		this.port = port;
		this.id = id;
	}

	public SSHIdentity(@NonNull JSONObject identity) {
		this.name = identity.optString("name");
		this.host = identity.optString("host");
		this.username = identity.optString("username");
		this.password = identity.optString("password");
		this.keyFilePath = identity.optString("keyFilePath");
		this.keyFilePassword = identity.optString("keyFilePassword");
		this.port = identity.optInt("port");
		this.id = identity.optLong("id");
	}

	public static JSONObject loadSavedIdentities(Context context, boolean overwrite) throws IOException, DataFormatException, JSONException {
		File file = context.getFileStreamPath("identities");
		if (!file.exists())
			//noinspection ResultOfMethodCallIgnored
			file.createNewFile();

		byte[] content = Compressor.readFile(file);

		if (!overwrite && content.length > 0) {
			String data = new String(Compressor.decompress(content), "UTF-8");
			return new JSONObject(data);
		} else
			return new JSONObject().put("identities", new JSONArray());
	}

	public static void saveIdentities(Context context, JSONObject data) throws IOException {
		Compressor.writeFile(context.getFileStreamPath("identities"), data.toString().getBytes("UTF-8"));
	}

	public static SSHIdentity fromID(Context context, long id) throws JSONException, IOException, DataFormatException {
		JSONArray savedIdentities = SSHIdentity.loadSavedIdentities(context, false).getJSONArray("identities");
		for (int i = 0; i < savedIdentities.length(); i++) {
			JSONObject identityObj = savedIdentities.getJSONObject(i);
			if (identityObj == null)
				continue;
			if (identityObj.getLong("id") == id) {
				return new SSHIdentity(
						identityObj.getString("name"),
						identityObj.getString("host"),
						identityObj.getString("username"),
						identityObj.optString("password"),
						identityObj.optString("keyFilePath"),
						identityObj.optString("keyFilePassword"),
						identityObj.getInt("port"),
						identityObj.getLong("id")
				);
			}
		}
		return new SSHIdentity();
	}

	@NonNull
	public String getName() {
		return name;
	}

	public void setName(@NonNull String name) {
		this.name = name;
	}

	@NonNull
	public String getHost() {
		return host;
	}

	public void setHost(@NonNull String host) {
		this.host = host;
	}

	@NonNull
	public String getUsername() {
		return username;
	}

	public void setUsername(@NonNull String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyFilePath() {
		return keyFilePath;
	}

	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}

	public String getKeyFilePassword() {
		return keyFilePassword;
	}

	public void setKeyFilePassword(String keyFilePassword) {
		this.keyFilePassword = keyFilePassword;
	}

	@NonNull
	public Integer getPort() {
		return port;
	}

	public void setPort(@NonNull Integer port) {
		this.port = port;
	}

	@NonNull
	public Long getId() {
		return id;
	}

	public void setID(@NonNull Long id) {
		this.id = id;
	}

	public JSONObject toJSONObject() {
		JSONObject result = new JSONObject();
		try {
			result.put("name", name);
			result.put("host", host);
			result.put("username", username);
			result.put("password", password);
			result.put("keyFilePath", keyFilePath);
			result.put("keyFilePassword", keyFilePassword);
			result.put("port", port);
			result.put("id", id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}