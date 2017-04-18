package com.sapuseven.noticap.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author paul
 * @version 1.0
 * @since 2017-04-04
 */

public class SSHClient {
	public static class RemoteCommand extends AsyncTask<Void, Void, ArrayList<String>> {
		private final String command;
		private final SSHIdentity identity;

		public RemoteCommand(String command, SSHIdentity identity) {
			this.command = command;
			this.identity = identity;
		}

		@Override
		protected ArrayList<String> doInBackground(Void... voids) {
			ArrayList<String> result = new ArrayList<>();
			try {
				JSch jsch = new JSch();
				Session session;
				if (identity.getKeyFilePath() != null && identity.getKeyFilePath().length() > 0) {
					jsch.addIdentity(identity.getKeyFilePath(), identity.getKeyFilePassword());
					session = jsch.getSession(identity.getUsername(), identity.getHost(), identity.getPort());
				} else {
					session = jsch.getSession(identity.getUsername(), identity.getHost(), identity.getPort());
					session.setPassword(identity.getPassword());
				}

				// This disables the unknown host confirmation.
				// TODO: Manage unknown hosts
				// See http://www.jcraft.com/jsch/examples/KnownHosts.java for an example
				Properties prop = new Properties();
				prop.put("StrictHostKeyChecking", "no");
				session.setConfig(prop);
				session.setTimeout(10000);

				session.connect();

				ChannelExec channel = (ChannelExec) session.openChannel("exec");

				channel.setCommand(command);

				InputStream in = channel.getInputStream();

				channel.connect();

				byte[] tmp = new byte[1024];
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0) break;
						result.add(new String(tmp, 0, i));
					}

					if (channel.isClosed()) {
						if (in.available() > 0) continue;
						result.add(String.valueOf(channel.getExitStatus()));
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				channel.disconnect();
			} catch (JSchException | IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<String> strings) {
			for (String line : strings)
				Log.i("SSH Response", line); // TODO: add some options to process the response (e.g. Log File)
		}
	}
}
