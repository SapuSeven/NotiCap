package com.sapuseven.noticap.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

public class FilterRule {
	@NonNull
	private String name = "";
	@NonNull
	private String[] packageNames = new String[0];
	@NonNull
	private Boolean useDaytime = false;
	private String from;
	private String to;
	@NonNull
	private Long identityID = 0L;
	@NonNull
	private String exec = "";
	private int minTimeDiff = 1000;

	public FilterRule() {
	}

	public FilterRule(@NonNull JSONObject rule) throws JSONException {
		name = rule.getString("name");
		ArrayList<String> packageNames = new ArrayList<>();
		for (int i = 0; i < rule.getJSONArray("packageNames").length(); i++) {
			try {
				packageNames.add(rule.getJSONArray("packageNames").getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		this.packageNames = packageNames.toArray(this.packageNames);
		useDaytime = rule.getBoolean("useDaytime");
		if (useDaytime) {
			from = rule.getString("from");
			to = rule.getString("to");
		}
		identityID = rule.getLong("identityID");
		exec = rule.getString("exec");
	}

	public static void saveRules(Context context, JSONObject data) throws IOException {
		Compressor.writeFile(context.getFileStreamPath("rules"), data.toString().getBytes("UTF-8"));
	}

	public static JSONObject loadSavedFilterRules(Context context, boolean overwrite) throws IOException, DataFormatException, JSONException {
		File file = context.getFileStreamPath("rules");
		if (!file.exists())
			//noinspection ResultOfMethodCallIgnored
			file.createNewFile();

		byte[] content = Compressor.readFile(file);

		if (!overwrite && content.length > 0) {
			String data = new String(Compressor.decompress(content), "UTF-8");
			return new JSONObject(data);
		} else
			return new JSONObject().put("rules", new JSONArray());
	}

	@NonNull
	public String getName() {
		return name;
	}

	public void setName(@NonNull String name) {
		this.name = name;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject result = new JSONObject();
		result.put("name", name);
		result.put("packageNames", new JSONArray(packageNames));
		result.put("useDaytime", useDaytime);
		if (useDaytime) {
			result.put("from", from);
			result.put("to", to);
		}
		result.put("identityID", identityID);
		result.put("exec", exec);
		return result;
	}

	public int getminTimeDiff(){
		return minTimeDiff;
	}

	@NonNull
	public String[] getPackageNames() {
		return packageNames;
	}

	public void setPackageNames(@NonNull String[] packageNames) {
		this.packageNames = packageNames;
	}

	@NonNull
	public Boolean useDaytime() {
		return useDaytime;
	}

	public void setUseDaytime(@NonNull Boolean useDaytime) {
		this.useDaytime = useDaytime;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@NonNull
	public Long getIdentityID() {
		return identityID;
	}

	public void setIdentityID(@NonNull Long identityID) {
		this.identityID = identityID;
	}

	@NonNull
	public String getExec() {
		return exec;
	}

	public void setExec(@NonNull String exec) {
		this.exec = exec;
	}
}